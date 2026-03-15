package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationality;
    private Boolean preferencesEnabled;
    private String role;
}
