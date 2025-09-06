package com.goormthon.careroad.nhis.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "nhis_facilities")
@Getter @Setter
public class NhisFacility {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    private String address;

    @Column(length = 50)
    private String code;
}
