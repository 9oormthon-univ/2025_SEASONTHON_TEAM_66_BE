package com.goormthon.careroad.reviews;

import com.goormthon.careroad.async.AsyncJobs;
import com.goormthon.careroad.events.ReviewEvents;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventHandler {

    private final AsyncJobs jobs;

    public ReviewEventHandler(AsyncJobs jobs) {
        this.jobs = jobs;
    }

    @Async("ioExecutor")
    @EventListener
    public void onCreated(ReviewEvents.Created e) {
        jobs.sendAuditLog("review.created id=" + e.reviewId() + " facility=" + e.facilityId());
        jobs.recomputeAggregation(e.facilityId().toString());
    }

    @Async("ioExecutor")
    @EventListener
    public void onUpdated(ReviewEvents.Updated e) {
        jobs.sendAuditLog("review.updated id=" + e.reviewId());
    }

    @Async("ioExecutor")
    @EventListener
    public void onDeleted(ReviewEvents.Deleted e) {
        jobs.sendAuditLog("review.deleted id=" + e.reviewId());
    }
}
