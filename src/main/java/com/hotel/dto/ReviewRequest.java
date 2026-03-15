package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ReviewRequest {

    @NotNull(message = "预订ID不能为空")
    private Long bookingId;

    @Min(value = 1, message = "评分至少为1星")
    @Max(value = 5, message = "评分最多为5星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 500, message = "评价内容长度在10-500字符之间")
    private String comment;
}
