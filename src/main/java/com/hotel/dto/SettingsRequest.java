package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class SettingsRequest {

    @NotBlank(message = "酒店名称不能为空")
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String hotelName;

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String contactEmail;

    @NotBlank(message = "电话号码不能为空")
    @Size(max = 20, message = "电话号码不能超过20个字符")
    private String contactPhone;

    @Size(max = 200, message = "地址不能超过200个字符")
    private String address;

    @NotBlank(message = "货币不能为空")
    @Pattern(regexp = "CNY|USD|EUR|GBP", message = "货币必须是 CNY, USD, EUR 或 GBP")
    private String currency;

    @NotBlank(message = "时区不能为空")
    @Pattern(regexp = "UTC\\+8|UTC\\+0|UTC-5", message = "时区必须是 UTC+8, UTC+0 或 UTC-5")
    private String timezone;

    @NotBlank(message = "语言不能为空")
    @Pattern(regexp = "Chinese|English|Spanish", message = "语言必须是 Chinese, English 或 Spanish")
    private String language;

    private Boolean twoFactorEnabled;

    @Min(value = 5, message = "会话超时不能少于5分钟")
    @Max(value = 120, message = "会话超时不能超过120分钟")
    private Integer sessionTimeout;

    @Min(value = 30, message = "密码有效期不能少于30天")
    @Max(value = 365, message = "密码有效期不能超过365天")
    private Integer passwordExpiry;

    private Boolean emailNotificationBookings;
    private Boolean emailNotificationCancellations;
    private Boolean pushNotificationsEnabled;
}
