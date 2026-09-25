package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.ExerciceCreationDto;
import cm.kfokam48.backend.dto.ExerciceCreeDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Exercice;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.StatutExercice;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.ExerciceRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;

/**
 * EF5 — dépôt du lien d'exercice.
 *
 * RG9/Q12/C2 (section 7) : le dépôt reste possible **après l'expiration du
 * code de présence** — l'expiration (RG1) ne concerne que la présence — mais
 * **pas après la clôture** de la session, qui fige tout.
 * L'assignation du relecteur (EF7) est le ticket #8 : ici, l'exercice reste
 * EN_ATTENTE (D4).
 */
@Service
public class ServiceExercice {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final ExerciceRepository exercices;
    private final Clock horloge;

    public ServiceExercice(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           ExerciceRepository exercices, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.exercices = exercices;
        this.horloge = horloge;
    }

    @Transactional
    public ExerciceCreeDto deposer(ExerciceCreationDto demande) {
        SessionCours session = sessions.findById(demande.sessionId())
                .orElseThrow(() -> new ApiException("SESSION_INCONNUE",
                        "Cette session n'existe pas.", 404));

        // RG9 (Q12) : jusqu'à la clôture, même si le code de présence a expiré
        if (session.estCloturee()) {
            throw new ApiException("SESSION_CLOTUREE",
                    "La session est clôturée : le dépôt est terminé.", 409);
        }

        Etudiant etudiant = etudiants.findById(demande.etudiantId())
                .orElseThrow(() -> new ApiException("ETUDIANT_INCONNU",
                        "Cet étudiant n'existe pas.", 404));

        if (exercices.existsBySessionIdAndEtudiantId(session.id, etudiant.id)) {
            throw new ApiException("EXERCICE_DEJA_DEPOSE",
                    "Cet étudiant a déjà déposé son exercice pour cette session.", 409);
        }

        validerLien(demande.lien());

        Exercice exercice = new Exercice();
        exercice.session = session;
        exercice.etudiant = etudiant;
        exercice.lien = demande.lien().trim();
        exercice.statut = StatutExercice.EN_ATTENTE; // D4 : l'assignation (EF7) vient au ticket #8
        exercice.deposeAt = horloge.instant();
        exercice = exercices.save(exercice);

        return new ExerciceCreeDto(exercice.id, exercice.statut.name());
    }

    /** 400 LIEN_INVALIDE (contrat) : le lien doit être une URI absolue http(s). */
    private void validerLien(String lien) {
        try {
            URI uri = URI.create(lien.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new ApiException("LIEN_INVALIDE",
                        "Le lien doit être une adresse http(s) valide.", 400);
            }
        } catch (IllegalArgumentException e) {
            throw new ApiException("LIEN_INVALIDE",
                    "Le lien doit être une adresse http(s) valide.", 400);
        }
    }
}
