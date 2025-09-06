package com.goormthon.careroad.facilities;

import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "facilities",
        indexes = {
                @Index(name = "idx_facilities_name", columnList = "name"),
                @Index(name = "idx_facilities_grade", columnList = "grade")
        })
public class Facility extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 10)
    private Grade grade;

    @Column(name = "capacity")
    private Integer capacity;

    public Facility() { this.id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
