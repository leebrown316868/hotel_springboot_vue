package com.hotel.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_unread", columnList = "userId, isRead"),
    @Index(name = "idx_user_created", columnList = "userId, createdAt")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, name = "user_id")
    private String userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(length = 200)
    private String actionLink;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
        if (priority == null) {
            priority = 0;
        }
    }
}
