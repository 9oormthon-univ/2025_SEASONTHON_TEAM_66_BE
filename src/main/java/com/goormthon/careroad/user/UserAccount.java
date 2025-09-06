package com.goormthon.careroad.user;

import com.goormthon.careroad.common.crypto.EncryptedStringConverter;
import com.goormthon.careroad.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users", indexes = @Index(name = "uk_users_email", columnList = "email", unique = true))
public class UserAccount extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 32, nullable = false)
    private Role role = Role.USER;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone_enc")
    private String phone; // 암호화 저장

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "realname_enc")
    private String realname; // 암호화 저장

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
