package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.SessionClotureeDto;
import cm.kfokam48.backend.dto.SessionCreeDto;
import cm.kfokam48.backend.dto.SessionCreationDto;
import cm.kfokam48.backend.entity.Promotion;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.PromotionRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class ServiceSession {

    /** RG1 — un code de présence expire 15 minutes après l'ouverture de la session (Q2). */
    public static final Duration DUREE_CODE = Duration.ofMinutes(15);

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // sans I, L, O, 0, 1 : lisibles au tableau

    private final SessionCoursRepository sessions;
    private final PromotionRepository promotions;
    private final Clock horloge;
    private final SecureRandom alea = new SecureRandom();

    public ServiceSession(SessionCoursRepository sessions, PromotionRepository promotions, Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.horloge = horloge;
    }

    @Transactional
    public SessionCreeDto ouvrir(SessionCreationDto demande) {
        Promotion promotion = promotions.findById(demande.promotionId())
                .orElseThrow(() -> new ApiException("PROMOTION_INCONNUE",
                        "La promotion indiquée n'existe pas.", 404));

        SessionCours session = new SessionCours();
        session.promotion = promotion;
        session.titre = demande.titre().trim();
        session.ouvertureAt = horloge.instant();
        session.expirationAt = session.ouvertureAt.plus(DUREE_CODE); // RG1
        session.code = genererCode();

        session = sessions.save(session);
        return new SessionCreeDto(session.id, session.code, session.ouvertureAt, session.expirationAt);
    }

    /**
     * EF12/RG8/RG13 : le formateur clôture la session.
     * 404 SESSION_INCONNUE — 409 SESSION_DEJA_CLOTUREE.
     * Après clôture : présence, dépôt, remplacement de lien et correction de note sont refusés.
     */
    @Transactional
    public SessionClotureeDto cloturer(Long sessionId) {
        SessionCours session = sessions.findById(sessionId)
                .orElseThrow(() -> new ApiException("SESSION_INCONNUE",
                        "Cette session n'existe pas.", 404));

        if (session.estCloturee()) {
            throw new ApiException("SESSION_DEJA_CLOTUREE",
                    "Cette session est déjà clôturée.", 409);
        }

        Instant clotureAt = horloge.instant();
        session.clotureAt = clotureAt;
        sessions.save(session);

        return new SessionClotureeDto(session.id, clotureAt);
    }

    private String genererCode() {
        for (;;) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHABET.charAt(alea.nextInt(ALPHABET.length())));
            }
            String code = sb.toString();
            if (sessions.findByCode(code).isEmpty()) {
                return code;
            }
        }
    }
}
