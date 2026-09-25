package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.PresenceCreeDto;
import cm.kfokam48.backend.dto.PresenceCreationDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Presence;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.SourcePresence;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.PresenceRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Flux « marquer sa présence » (EF2). Les erreurs correspondent au contrat :
 * 400 CODE_INCONNU (issue #4), 410 CODE_EXPIRE (issue #4), 409 DEJA_PRESENT (issue #3),
 * 400 BLOCAGE_ACTIF (issue #5). Les branches non nominales de ce service sont
 * complétées par les issues suivantes — ce ticket pose le cas nominal.
 */
@Service
public class ServicePresence {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final Clock horloge;

    public ServicePresence(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           PresenceRepository presences, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.horloge = horloge;
    }

    @Transactional
    public PresenceCreeDto marquer(PresenceCreationDto demande) {
        // RG1 — le code désigne une session ouverte
        SessionCours session = sessions.findByCode(demande.code())
                .orElseThrow(() -> new ApiException("CODE_INCONNU",
                        "Ce code de présence ne correspond à aucune session.", 400));
        if (session.estCloturee() || horloge.instant().isAfter(session.expirationAt)) {
            throw new ApiException("CODE_EXPIRE",
                    "Le code de présence a expiré.", 410);
        }

        Etudiant etudiant = etudiants.findById(demande.etudiantId())
                .orElseThrow(() -> new ApiException("ETUDIANT_INCONNU",
                        "Cet étudiant n'existe pas.", 404));

        Presence presence = new Presence();
        presence.session = session;
        presence.etudiant = etudiant;
        presence.source = SourcePresence.ETUDIANT;
        presence.creeAt = horloge.instant();
        presence = presences.save(presence);

        return new PresenceCreeDto(presence.id, session.id, etudiant.id, presence.source.name());
    }
}
