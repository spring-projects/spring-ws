package org.springframework.ws.observation;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is executed by using observation Maven profile and explicitly excluding the default profile.
 * This test relies on dependencies that cause problems with other tests in Spring WS Core.
 * @author Corneil du Plessis
 */
@Profile("observation")
@SpringBootTest(classes = ObservationInWsConfigurerTests.WsTracingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ObservationInWsConfigurerTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void responseShouldNotBeEmpty() {
        final ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/test", String.class);
        assertThat(response.getBody()).isNotEmpty();
    }

    @RestController
    public static class TestEndpoint {
        private static final Logger log = LoggerFactory.getLogger(TestEndpoint.class);

        @GetMapping("/test")
        public String test() {
            log.info("test");
            return MDC.get("spanId");
        }
    }

    @Configuration
    public static class WsConfig extends WsConfigurerAdapter {
        private final ObservationRegistry observationRegistry;

        public WsConfig(ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
        }
    }

    @SpringBootApplication
    @Import(WsConfig.class)
    public static class WsTracingApplication {
        public static void main(String[] args) {
            SpringApplication.run(WsTracingApplication.class, args);
        }

    }
}
