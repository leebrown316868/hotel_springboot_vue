package com.hotel.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "guests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GuestStatus status = GuestStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalBookings = 0;

    @Column
    private LocalDate lastStay;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = GuestStatus.ACTIVE;
        }
        if (totalBookings == null) {
            totalBookings = 0;
        }
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
