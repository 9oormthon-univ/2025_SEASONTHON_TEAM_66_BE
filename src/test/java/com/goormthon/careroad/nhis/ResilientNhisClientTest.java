package com.goormthon.careroad.nhis;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ResilientNhisClientTest {

    static com.github.tomakehurst.wiremock.WireMockServer wm;

    @BeforeAll
    static void up() {
        wm = new com.github.tomakehurst.wiremock.WireMockServer(0);
        wm.start();
        WireMock.configureFor("localhost", wm.port());
    }

    @AfterAll
    static void down() { wm.stop(); }

    @Test
    void retry_then_success() {
        stubFor(get(urlEqualTo("/facilities?page=1&size=10"))
                .inScenario("retry")
                .whenScenarioStateIs("STARTED")
                .willReturn(aResponse().withStatus(500)));

        stubFor(get(urlEqualTo("/facilities?page=1&size=10"))
                .inScenario("retry")
                .whenScenarioStateIs("STARTED")
                .willSetStateTo("ok")
                .willReturn(aResponse().withStatus(500)));

        stubFor(get(urlEqualTo("/facilities?page=1&size=10"))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(aResponse().withStatus(200).withBody("[]")));

        new ApplicationContextRunner()
                .withPropertyValues(
                        "app.nhis.enabled=true",
                        "resilience4j.retry.nhis.max-attempts=3")
                .withBean(NhisProperties.class, () -> {
                    NhisProperties p = new NhisProperties();
                    p.setBaseUrl("http://localhost:" + wm.port());
                    return p;
                })
                .withBean(RestClient.class, RestClient::create)
                .withBean(ResilientNhisClient.class)
                .run(ctx -> {
                    NhisClient client = ctx.getBean(NhisClient.class);
                    var list = client.fetchFacilities(1,10);
                    assertThat(list).isEmpty();
                });
    }
}
