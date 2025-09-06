package com.goormthon.careroad.messaging;

public interface EventPublisher {
    void publish(String eventType, String key, String payload) throws Exception;
}
