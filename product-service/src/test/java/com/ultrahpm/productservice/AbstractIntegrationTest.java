package com.ultrahpm.productservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    );

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    @ServiceConnection(name = "elasticsearch")
    static GenericContainer<?> elasticsearch = new GenericContainer<>(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.1")
    )
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withExposedPorts(9200);

    @BeforeAll
    static void beforeAll() {
        // Any custom setup before all tests
    }
}
