package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.RelectureEnAttenteDto;
import cm.kfokam48.backend.dto.RelectureRendueDto;
import cm.kfokam48.backend.dto.ResultatExerciceDto;
import cm.kfokam48.backend.entity.Exercice;
import cm.kfokam48.backend.entity.Relecture;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.StatutExercice;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.ExerciceRepository;
import cm.kfokam48.backend.repository.RelectureRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

 /**
  * EF8 — le relecteur rend sa note et son commentaire.
  * EF9 — le relecteur corrige sa note tant que la session n'est pas clôturée (RG13, C1).
  * RG5 — deux relecteurs distincts par exercice (contrainte UK sur relecture.exercice_id, numero).
  * RG7 — note entière entre 0 et 20 (Q9).
  * RG10 — l'auteur ne voit jamais le nom du relecteur (Q8).
  * RG13 — correction possible jusqu'à la clôture, après plus rien n'est modifiable (Q10, C1).
  * RG16 — note PROVISIONAL (1 évaluation) / FINAL (2 évaluations = moyenne).
  * RG17 — même pair ne peut pas être assigné deux fois.
  */
@Service
public class ServiceRelecture {

    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final SessionCoursRepository sessions;
    private final Clock horloge;

    public ServiceRelecture(ExerciceRepository exercices, RelectureRepository relectures,
                            SessionCoursRepository sessions, Clock horloge) {
        this.exercices = exercices;
        this.relectures = relectures;
        this.sessions = sessions;
        this.horloge = horloge;
    }

    /**
     * POST /api/relectures/{id} — EF8 : le relecteur rend sa note et son commentaire.
     * 400 NOTE_INVALIDE si note non entière ou hors 0–20 (RG7).
     * 403 AUTO_RELECTURE si l'étudiant essaie de relire son propre exercice (RG4).
     * 409 RELECTURE_DEJA_RENDUE si cette relecture (numero) a déjà été rendue.
     * 409 RELECTEUR_DEJA_ASSIGNE si le même relecteur essaie de faire les deux relectures (RG17).
     */
    @Transactional
    public void rendre(Long relectureId, Long relecteurId, RelectureRendueDto demande) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> new ApiException("RELECTURE_INCONNUE",
                        "Cette relecture n'existe pas.", 404));

        if (relecture.rendueAt != null) {
            throw new ApiException("RELECTURE_DEJA_RENDUE",
                    "Cette relecture a déjà été rendue.", 409);
        }

        // RG4 : l'auteur ne peut pas relire son propre exercice
        if (relecture.exercice.etudiant.id.equals(relecteurId)) {
            throw new ApiException("AUTO_RELECTURE",
                    "Vous ne pouvez pas relire votre propre exercice.", 403);
        }

        // RG17 : vérifier que ce relecteur n'a pas déjà rendu l'autre relecture
        if (relectures.existsByExerciceIdAndRelecteurId(relecture.exercice.id, relecteurId)) {
            throw new ApiException("RELECTEUR_DEJA_ASSIGNE",
                    "Vous avez déjà rendu une relecture pour cet exercice.", 409);
        }

        // RG7 : note entière entre 0 et 20 (validé par @Min/@Max dans le DTO)
        relecture.note = demande.note();
        relecture.commentaire = demande.commentaire().trim();
        relecture.rendueAt = horloge.instant();

        // Vérifier si les deux relectures sont rendues pour passer en RELU
        verifierEtMettreAJourStatutExercice(relecture.exercice);
    }

    /**
     * Vérifie si les deux relectures sont rendues et met à jour le statut de l'exercice.
     * Si 1 relecture rendue : statut reste ASSIGNE (note PROVISIONAL)
     * Si 2 relectures rendues : statut -> RELU (note FINAL = moyenne)
     */
    private void verifierEtMettreAJourStatutExercice(Exercice exercice) {
        long nbRendues = exercice.relectures.stream()
                .filter(r -> r.rendueAt != null)
                .count();

        if (nbRendues == 2) {
            exercice.statut = StatutExercice.RELU;
        }
        // Si 1 seule rendue : reste ASSIGNE (note PROVISIONAL)
        exercices.save(exercice);
    }

    /**
     * POST /api/relectures/{id}/correction — EF9 : le relecteur corrige sa note (RG13, C1).
     * 409 SESSION_CLOTUREE si la session est clôturée (RG13).
     * 404 RELECTURE_INCONNUE si la relecture n'existe pas.
     * 403 AUTO_RELECTURE si ce n'est pas le bon relecteur.
     */
    @Transactional
    public void corriger(Long relectureId, Long relecteurId, RelectureRendueDto demande) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> new ApiException("RELECTURE_INCONNUE",
                        "Cette relecture n'existe pas.", 404));

        if (relecture.rendueAt == null) {
            throw new ApiException("RELECTURE_DEJA_RENDUE",
                    "Aucune relecture rendue à corriger.", 409);
        }

        // RG13 : après clôture, plus rien n'est modifiable
        SessionCours session = relecture.exercice.session;
        if (session.estCloturee()) {
            throw new ApiException("SESSION_CLOTUREE",
                    "La session est clôturée : la correction n'est plus possible.", 409);
        }

        // Vérifier que c'est bien le relecteur qui corrige
        if (!relecture.relecteur.id.equals(relecteurId)) {
            throw new ApiException("AUTO_RELECTURE",
                    "Vous n'êtes pas le relecteur de cet exercice.", 403);
        }

        relecture.note = demande.note();
        relecture.commentaire = demande.commentaire().trim();
        // rendueAt ne change pas
    }

    /**
     * GET /api/relectures/en-attente — relectures qu'un étudiant doit encore rendre (EF8, RG14).
     * Retourne les relectures assignées non rendues (exercice, auteur masqué, lien, numero).
     */
    @Transactional(readOnly = true)
    public List<RelectureEnAttenteDto> enAttente(Long relecteurId) {
        List<Relecture> liste = relectures.findByRelecteurIdAndRendueAtIsNull(relecteurId);
        return liste.stream().map(r -> new RelectureEnAttenteDto(
                r.id,
                r.exercice.id,
                r.numero,
                r.exercice.lien,
                r.exercice.session.titre
        )).toList();
    }

    /**
     * GET /api/etudiants/{id}/resultats — notes et commentaires reçus par l'étudiant (EF11, RG10, RG16).
     * L'auteur ne voit jamais le nom du relecteur (RG10).
     * Statut note : AUCUNE / PROVISIONAL (1 éval) / FINAL (2 évals = moyenne).
     */
    @Transactional(readOnly = true)
    public List<ResultatExerciceDto> resultats(Long etudiantId) {
        return exercices.findByEtudiantId(etudiantId).stream().map(e -> {
            List<Relecture> rendues = e.relectures.stream()
                    .filter(r -> r.rendueAt != null)
                    .toList();

            int nbEvaluations = rendues.size();
            String statutNote;
            Double moyenne = null;
            Integer noteUnique = null;
            String commentaireUnique = null;

            if (nbEvaluations == 0) {
                statutNote = "AUCUNE";
            } else if (nbEvaluations == 1) {
                statutNote = "PROVISIONAL";
                noteUnique = rendues.get(0).note;
                commentaireUnique = rendues.get(0).commentaire;
                moyenne = noteUnique != null ? noteUnique.doubleValue() : null;
            } else { // 2 évaluations
                statutNote = "FINAL";
                moyenne = rendues.stream()
                        .mapToInt(r -> r.note != null ? r.note : 0)
                        .average()
                        .orElse(0.0);
            }

            return new ResultatExerciceDto(
                    e.id,
                    e.session.titre,
                    e.statut.name(),
                    statutNote,
                    nbEvaluations,
                    moyenne,
                    noteUnique,
                    commentaireUnique,
                    nbEvaluations == 2 ? rendues.stream()
                            .map(r -> new ResultatExerciceDto.EvaluationDto(r.note, r.commentaire))
                            .toList() : List.of()
            );
        }).toList();
    }
}