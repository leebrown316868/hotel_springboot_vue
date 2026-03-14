package com.hotel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatus {
    AVAILABLE("空闲"),
    OCCUPIED("已入住"),
    CLEANING("清洁中"),
    MAINTENANCE("维修中");

    private final String displayName;
}
