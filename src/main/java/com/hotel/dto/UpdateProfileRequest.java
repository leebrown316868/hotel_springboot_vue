package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;

    private String address;

    private String nationality;

    private Boolean preferencesEnabled;
}
