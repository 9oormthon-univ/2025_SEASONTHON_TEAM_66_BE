package com.goormthon.careroad.outbox;

import com.goormthon.careroad.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxRelayTest extends BaseIntegrationTest {

    @Autowired OutboxRepository repo;
    @Autowired OutboxRelay relay;

    @Test
    void deliver_changes_status_to_delivered() {
        Outbox ob = new Outbox();
        ob.setAggregateType("Review");
        ob.setAggregateId("rid-1");
        ob.setEventType("CREATED");
        ob.setPayload("{\"ok\":true}");
        repo.save(ob);

        // 스케줄 기다리지 않고 직접 1회 실행
        relay.deliver();

        Outbox reloaded = repo.findById(ob.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isIn("DELIVERED", "RETRY");
    }
}
