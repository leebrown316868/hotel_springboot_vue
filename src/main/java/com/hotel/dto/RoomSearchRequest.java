package com.hotel.dto;

import com.hotel.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchRequest {

    @NotNull(message = "入住日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @NotNull(message = "退房日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    @NotNull(message = "入住人数不能为空")
    @Min(value = 1, message = "入住人数至少为1人")
    private Integer guestCount;

    // 接收逗号分隔的房型字符串（如："DOUBLE,EXECUTIVE_SUITE"）
    private String roomTypesStr;

    // 获取房型列表（将字符串转换为枚举列表）
    public List<RoomType> getRoomTypes() {
        if (roomTypesStr == null || roomTypesStr.trim().isEmpty()) {
            return null;
        }
        return List.of(roomTypesStr.split(","))
                .stream()
                .map(String::trim)
                .map(RoomType::valueOf)
                .collect(Collectors.toList());
    }
}
