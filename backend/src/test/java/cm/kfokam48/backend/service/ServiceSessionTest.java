package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.SessionCreeDto;
import cm.kfokam48.backend.dto.SessionCreationDto;
import cm.kfokam48.backend.entity.Promotion;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.PromotionRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * B6 — test unitaire d'une règle métier réelle : RG1, l'expiration du code à
 * 15 minutes (Q2). L'horloge est fixée : le test ne dépend pas de l'heure.
 */
@ExtendWith(MockitoExtension.class)
class ServiceSessionTest {

    @Mock
    private SessionCoursRepository sessions;
    @Mock
    private PromotionRepository promotions;

    private static final Instant MAINTENANT = Instant.parse("2026-09-25T10:00:00Z");

    @Test
    void leCodeExpireQuinzeMinutesApresLOuverture() {
        Promotion promo = new Promotion();
        promo.id = 1L;
        when(promotions.findById(1L)).thenReturn(Optional.of(promo));
        when(sessions.findByCode(any())).thenReturn(Optional.empty());
        when(sessions.save(any(SessionCours.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceSession service = new ServiceSession(sessions, promotions, Clock.fixed(MAINTENANT, ZoneOffset.UTC));

        SessionCreeDto cree = service.ouvrir(new SessionCreationDto("Algèbre", 1L));

        assertThat(cree.expirationAt()).isEqualTo(MAINTENANT.plus(Duration.ofMinutes(15)));
        assertThat(cree.ouvertureAt()).isEqualTo(MAINTENANT);
        assertThat(cree.code()).hasSize(6);
    }

    @Test
    void unePromotionInconnueEstRefusee() {
        when(promotions.findById(anyLong())).thenReturn(Optional.empty());

        ServiceSession service = new ServiceSession(sessions, promotions, Clock.fixed(MAINTENANT, ZoneOffset.UTC));

        assertThatThrownBy(() -> service.ouvrir(new SessionCreationDto("Algèbre", 99L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("n'existe pas");
    }
}
