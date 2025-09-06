package com.goormthon.careroad.events;

import java.util.UUID;

public final class ReviewEvents {
    private ReviewEvents() {}

    public record Created(UUID reviewId, UUID facilityId, String userRef, int rating) {}
    public record Updated(UUID reviewId, String userRef) {}
    public record Deleted(UUID reviewId, String userRef) {}
}
