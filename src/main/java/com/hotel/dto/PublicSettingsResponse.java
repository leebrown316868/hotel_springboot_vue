package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicSettingsResponse {
    private String hotelName;
    private String description;
    private String address;
    private String contactPhone;
    private String contactEmail;
}
