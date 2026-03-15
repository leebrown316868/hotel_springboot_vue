package com.hotel.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 酒店基本信息
    @Column(nullable = false, length = 100)
    private String hotelName = "GrandHorizon 酒店及水疗中心";

    @Column(nullable = false, length = 100)
    private String contactEmail = "contact@grandhorizon.com";

    @Column(nullable = false, length = 20)
    private String contactPhone = "+1 (555) 123-4567";

    @Column(length = 200)
    private String address = "123 Ocean Drive, Miami, FL 33139";

    @Column(nullable = false, length = 10)
    private String currency = "CNY";

    @Column(nullable = false, length = 10)
    private String timezone = "UTC+8";

    @Column(nullable = false, length = 20)
    private String language = "Chinese";

    // 安全设置
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(nullable = false)
    private Integer sessionTimeout = 30;

    @Column(nullable = false)
    private Integer passwordExpiry = 90;

    // 通知设置
    @Column(nullable = false)
    private Boolean emailNotificationBookings = true;

    @Column(nullable = false)
    private Boolean emailNotificationCancellations = true;

    @Column(nullable = false)
    private Boolean pushNotificationsEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 50)
    private String updatedBy = "system";

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
