package com.goormthon.careroad.payments;

import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.common.jpa.BaseTimeEntity;
import com.goormthon.careroad.facilities.Facility;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_facility_grade", columnNames = {"facility_id", "grade"}),
        indexes = @Index(name = "idx_payments_facility", columnList = "facility_id"))
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // bigserial

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 10, nullable = false)
    private Grade grade;

    @Column(name = "daily_cost", precision = 12, scale = 2)
    private BigDecimal dailyCost;

    @Column(name = "monthly_cost", precision = 12, scale = 2)
    private BigDecimal monthlyCost;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }
    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public BigDecimal getDailyCost() { return dailyCost; }
    public void setDailyCost(BigDecimal dailyCost) { this.dailyCost = dailyCost; }
    public BigDecimal getMonthlyCost() { return monthlyCost; }
    public void setMonthlyCost(BigDecimal monthlyCost) { this.monthlyCost = monthlyCost; }
}
