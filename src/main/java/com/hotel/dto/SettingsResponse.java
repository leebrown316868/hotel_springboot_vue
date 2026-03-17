package com.hotel.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SettingsResponse {
    private Long id;
    private String hotelName;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String currency;
    private String timezone;
    private String language;
    private Boolean twoFactorEnabled;
    private Integer sessionTimeout;
    private Integer passwordExpiry;
    private Boolean emailNotificationBookings;
    private Boolean emailNotificationCancellations;
    private Boolean pushNotificationsEnabled;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
