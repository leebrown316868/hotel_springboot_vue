package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class BookingRequest {

    // 客人ID（如果提供新客人信息，此字段可忽略）
    private Long guestId;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;

    @NotNull(message = "退房日期不能为空")
    private LocalDate checkOutDate;

    @NotNull(message = "入住人数不能为空")
    @Min(value = 1, message = "入住人数至少为1人")
    private Integer guestCount;

    // 客人信息（可选，用于创建新客人或更新现有客人信息）
    private GuestInfo guestInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestInfo {
        private String name;
        private String phone;
        private String email;
        private String notes;
    }
}
