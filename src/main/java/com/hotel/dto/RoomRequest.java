package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class RoomRequest {

    @NotBlank(message = "房间号不能为空")
    private String number;

    @NotBlank(message = "楼层不能为空")
    private String floor;

    @NotBlank(message = "房型不能为空")
    private String type;

    @NotBlank(message = "状态不能为空")
    private String status;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
}
