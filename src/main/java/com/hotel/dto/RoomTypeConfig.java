package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeConfig {
    @NotBlank(message = "房型名称不能为空")
    @Size(min = 1, max = 50, message = "房型名称长度必须在1-50之间")
    private String name;

    @Min(value = 1, message = "容纳人数至少为1人")
    @Max(value = 10, message = "容纳人数最多为10人")
    private Integer capacity;

    @Min(value = 0, message = "基础价格不能为负数")
    @DecimalMax(value = "10000.00", message = "基础价格不能超过10000元")
    private BigDecimal basePrice;
}
