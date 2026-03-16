package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class RoomTypeRequest {

    @NotBlank(message = "房型代码不能为空")
    @Pattern(regexp = "^[A-Z_0-9]+$", message = "房型代码只能包含大写字母、下划线和数字")
    @Size(max = 50, message = "房型代码最多50个字符")
    private String code;

    @NotBlank(message = "房型名称不能为空")
    @Size(max = 100, message = "房型名称最多100个字符")
    private String name;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量至少为1")
    @Max(value = 20, message = "容量最多为20")
    private Integer capacity;

    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0.01", message = "基础价格必须大于0")
    @DecimalMax(value = "999999.99", message = "基础价格不能超过999999.99")
    private BigDecimal basePrice;
}
