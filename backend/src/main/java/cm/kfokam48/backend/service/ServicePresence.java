package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.PresenceCreeDto;
import cm.kfokam48.backend.dto.PresenceCreationDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Presence;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.SourcePresence;
import cm.kfokam48.backend.entity.TentativeErreur;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.PresenceRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import cm.kfokam48.backend.repository.TentativeErreurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Flux « marquer sa présence » (EF2), branches du D3 dans l'ordre du diagramme :
 * 1. blocage RG3 actif → 400 BLOCAGE_ACTIF (issue #5)
 * 2. code inconnu → 400 CODE_INCONNU (issue #4)
 * 3. code expiré ou session clôturée → 410 CODE_EXPIRE (issue #4, RG1)
 * 4. déjà présent → 409 DEJA_PRESENT (issue #3, RG2)
 * 5. nominal → 201 (issue #2, EF2)
 */
@Service
public class ServicePresence {

    /** RG1 — un code de présence expire 15 minutes après l'ouverture (Q2). */
    public static final Duration DUREE_CODE = Duration.ofMinutes(15);

    /** RG3 — après 5 codes erronés, l'étudiant est bloqué 2 minutes (Q4). */
    public static final int SEUIL_ERREURS = 5;
    public static final Duration DUREE_BLOCAGE = Duration.ofMinutes(2);

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final TentativeErreurRepository tentatives;
    private final Clock horloge;

    public ServicePresence(SessionCoursRepository sessions, EtudiantRepository etudiants,
                           PresenceRepository presences, TentativeErreurRepository tentatives,
                           Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.tentatives = tentatives;
        this.horloge = horloge;
    }

    @Transactional
    public PresenceCreeDto marquer(PresenceCreationDto demande) {
        Instant maintenant = horloge.instant();

        Etudiant etudiant = etudiants.findById(demande.etudiantId())
                .orElseThrow(() -> new ApiException("ETUDIANT_INCONNU",
                        "Cet étudiant n'existe pas.", 404));

        // branche 1 du D3 — RG3 : 5 erreurs en moins de 2 minutes → blocage
        long erreurs = tentatives.countByEtudiantIdAndSurvenueAtAfter(
                etudiant.id, maintenant.minus(DUREE_BLOCAGE));
        if (erreurs >= SEUIL_ERREURS) {
            throw new ApiException("BLOCAGE_ACTIF",
                    "Trop de codes erronés : réessayez dans deux minutes.", 400);
        }

        // branche 2 — code inconnu (comptabilisé comme tentative erronée, H6)
        SessionCours session = sessions.findByCode(demande.code())
                .orElseGet(() -> {
                    tentatives.save(nouvelleTentative(etudiant, maintenant));
                    throw new ApiException("CODE_INCONNU",
                            "Ce code de présence ne correspond à aucune session.", 400);
                });

        // branche 3 — code expiré ou session clôturée (RG1) ; compte aussi comme erreur (RG3)
        if (session.estCloturee() || maintenant.isAfter(session.expirationAt)) {
            tentatives.save(nouvelleTentative(etudiant, maintenant));
            throw new ApiException("CODE_EXPIRE", "Le code de présence a expiré.", 410);
        }

        // branche 4 — RG2 : la contrainte SQL tranche en cas de concurrence
        if (presences.existsBySessionIdAndEtudiantId(session.id, etudiant.id)) {
            throw new ApiException("DEJA_PRESENT",
                    "Cet étudiant a déjà marqué sa présence pour cette session.", 409);
        }

        // branche 5 — nominal : on repart sur un compteur propre
        tentatives.deleteByEtudiantId(etudiant.id);
        Presence presence = new Presence();
        presence.session = session;
        presence.etudiant = etudiant;
        presence.source = SourcePresence.ETUDIANT;
        presence.creeAt = maintenant;
        presence = presences.save(presence);

        return new PresenceCreeDto(presence.id, session.id, etudiant.id, presence.source.name());
    }

    private TentativeErreur nouvelleTentative(Etudiant etudiant, Instant survenueAt) {
        TentativeErreur t = new TentativeErreur();
        t.etudiant = etudiant;
        t.survenueAt = survenueAt;
        return t;
    }
}
