package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.ExerciceCreationDto;
import cm.kfokam48.backend.dto.ExerciceCreeDto;
import cm.kfokam48.backend.dto.ExerciceLienRemplaceDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Exercice;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.StatutExercice;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.ExerciceRepository;
import cm.kfokam48.backend.repository.PresenceRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * EF5 — dépôt du lien d'exercice.
 * EF6 — remplacement du lien tant que la relecture n'a pas commencé (RG11).
 *
 * RG9/Q12/C2 (section 7) : le dépôt reste possible **après l'expiration du
 * code de présence** — l'expiration (RG1) ne concerne que la présence — mais
 * **pas après la clôture** de la session, qui fige tout.
 * L'assignation du relecteur (EF7) se fait au dépôt : un présent au hasard,
 * jamais l'auteur (RG4, RG6). S'il n'y a pas d'autre présent, l'exercice
 * reste EN_ATTENTE (RG14).
 */
@Service
public class ServiceExercice {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final ExerciceRepository exercices;
    private final PresenceRepository presences;
    private final Clock horloge;
    private final SecureRandom alea = new SecureRandom();

    public ServiceExercice(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           ExerciceRepository exercices, PresenceRepository presences,
                           Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.exercices = exercices;
        this.presences = presences;
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
        exercice.statut = StatutExercice.EN_ATTENTE;
        exercice.deposeAt = horloge.instant();

        // EF7/RG6 : assigner un relecteur au hasard parmi les présents, sauf l'auteur
        assignerRelecteur(session.id, etudiant.id, exercice);

        exercice = exercices.save(exercice);

        return new ExerciceCreeDto(exercice.id, exercice.statut.name());
    }

    /**
     * EF6/RG11 : remplacer le lien tant que la relecture n'a pas commencé.
     * 404 EXERCICE_INCONNU si l'exercice n'existe pas ou n'appartient pas à l'étudiant.
     * 409 SESSION_CLOTUREE si la session est clôturée.
     * 409 LIEN_VERROUILLE si la relecture a commencé (statut ASSIGNE ou RELU).
     * 400 LIEN_INVALIDE si le lien n'est pas une URI http(s) valide.
     */
    @Transactional
    public ExerciceLienRemplaceDto remplacerLien(Long exerciceId, Long etudiantId, String nouveauLien) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> new ApiException("EXERCICE_INCONNU",
                        "Cet exercice n'existe pas.", 404));

        if (!exercice.etudiant.id.equals(etudiantId)) {
            throw new ApiException("EXERCICE_INCONNU",
                    "Cet exercice n'existe pas.", 404);
        }

        SessionCours session = exercice.session;
        if (session.estCloturee()) {
            throw new ApiException("SESSION_CLOTUREE",
                    "La session est clôturée : le lien ne peut plus être remplacé.", 409);
        }

        // RG11 : remplacement refusé dès que la relecture a commencé (ASSIGNE ou RELU)
        if (exercice.statut != StatutExercice.EN_ATTENTE) {
            throw new ApiException("LIEN_VERROUILLE",
                    "La relecture a commencé : le lien ne peut plus être remplacé.", 409);
        }

        validerLien(nouveauLien);

        exercice.lien = nouveauLien.trim();
        exercice.lienRemplaceAt = horloge.instant();
        exercice = exercices.save(exercice);

        return new ExerciceLienRemplaceDto(exercice.id, exercice.statut.name(), exercice.lien);
    }

    /**
     * RG6/Q7 : le relecteur est choisi au hasard parmi les étudiants présents
     * à la session, à l'exclusion de l'auteur (RG4). Si aucun autre présent,
     * l'exercice reste EN_ATTENTE (RG14).
     */
    private void assignerRelecteur(Long sessionId, Long auteurId, Exercice exercice) {
        List<Long> presents = presences.findEtudiantIdsBySessionId(sessionId);
        presents.remove(auteurId); // RG4 : pas de relecture de soi-même
        if (presents.isEmpty()) {
            return; // reste EN_ATTENTE, pas de relecteur possible
        }
        Long relecteurId = presents.get(alea.nextInt(presents.size()));
        Etudiant relecteur = etudiants.getReferenceById(relecteurId);
        exercice.relecteur = relecteur;
        exercice.statut = StatutExercice.ASSIGNE;
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
