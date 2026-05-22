package com.groupeat.domain.verification.phone.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime verifiedAt;

    public static PhoneVerification create(String phoneNumber, String code) {
        PhoneVerification verification = new PhoneVerification();
        verification.phoneNumber = phoneNumber;
        verification.code = code;
        verification.verified = false;
        verification.used = false;
        verification.expiredAt = LocalDateTime.now().plusMinutes(5);
        return verification;
    }

    public void verify() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void use() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public boolean isCodeMatched(String code) {
        return this.code.equals(code);
    }
}
