package com.goormthon.careroad.reviews;

import com.goormthon.careroad.events.ReviewEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewDomainPublisher {
    private final ApplicationEventPublisher publisher;
    public ReviewDomainPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
    public void publishCreated(UUID reviewId, UUID facilityId, String userRef, int rating) {
        publisher.publishEvent(new ReviewEvents.Created(reviewId, facilityId, userRef, rating));
    }
    public void publishUpdated(UUID reviewId, String userRef) {
        publisher.publishEvent(new ReviewEvents.Updated(reviewId, userRef));
    }
    public void publishDeleted(UUID reviewId, String userRef) {
        publisher.publishEvent(new ReviewEvents.Deleted(reviewId, userRef));
    }
}
