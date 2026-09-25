package cm.kfokam48.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge injectable : les règles temporelles (RG1 — expiration 15 min)
 * sont testables sans attendre ni simuler le temps système.
 */
@Configuration
public class ConfigHorloge {

    @Bean
    public Clock horloge() {
        return Clock.systemUTC();
    }
}
