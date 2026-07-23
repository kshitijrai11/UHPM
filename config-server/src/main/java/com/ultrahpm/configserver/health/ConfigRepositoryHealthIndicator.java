package com.ultrahpm.configserver.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ConfigRepositoryHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check Git repository connectivity
            boolean isGitReachable = checkGitRepository();

            if (isGitReachable) {
                return Health.up()
                    .withDetail("repository", "Git repository is accessible")
                    .withDetail("lastRefresh", System.currentTimeMillis())
                    .build();
            } else {
                return Health.down()
                    .withDetail("repository", "Git repository is not accessible")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    private boolean checkGitRepository() {
        // Implementation to check Git repository connectivity
        // In production, this would verify the Git repo is reachable
        return true;
    }
}
