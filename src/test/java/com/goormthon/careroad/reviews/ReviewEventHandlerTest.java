package com.goormthon.careroad.reviews;

import com.goormthon.careroad.async.AsyncJobs;
import com.goormthon.careroad.events.ReviewEvents;
import com.goormthon.careroad.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.annotation.Resource;

import static org.awaitility.Awaitility.await;
import java.time.Duration;

import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class ReviewEventHandlerTest extends BaseIntegrationTest {

    @Resource ApplicationEventPublisher publisher;

    @MockitoSpyBean
    AsyncJobs jobs;

    @Test
    void on_created_triggers_async_jobs() {
        publisher.publishEvent(new ReviewEvents.Created(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                "user@example.com",
                5));

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() ->
                        Mockito.verify(jobs, Mockito.atLeastOnce())
                                .sendAuditLog(Mockito.contains("review.created")));
    }
}
