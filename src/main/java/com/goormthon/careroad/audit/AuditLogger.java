package com.goormthon.careroad.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void loginSuccess(String user) {
        audit.info("login success user={}", user);
    }

    public void loginFail(String userOrEmail) {
        audit.info("login fail userOrEmail={}", userOrEmail);
    }

    public void reviewDeleted(String actor, String reviewId) {
        audit.info("review deleted actor={} reviewId={}", actor, reviewId);
    }
}
