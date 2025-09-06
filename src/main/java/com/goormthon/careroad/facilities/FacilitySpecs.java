package com.goormthon.careroad.facilities;

import com.goormthon.careroad.common.code.Grade;
import org.springframework.data.jpa.domain.Specification;

public final class FacilitySpecs {
    private FacilitySpecs() {}

    public static Specification<Facility> nameContains(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim() + "%";
        return (root, cq, cb) -> cb.like(root.get("name"), like);
    }

    public static Specification<Facility> gradeEquals(String grade) {
        if (grade == null || grade.isBlank()) return null;
        Grade g;
        try { g = Grade.valueOf(grade.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return null; } // 잘못된 값은 무시
        return (root, cq, cb) -> cb.equal(root.get("grade"), g);
    }
}
