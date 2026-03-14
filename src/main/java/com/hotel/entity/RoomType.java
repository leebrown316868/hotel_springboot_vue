package com.hotel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomType {
    SINGLE("单人间"),
    DOUBLE("双人间"),
    SUITE("套房"),
    EXECUTIVE_SUITE("行政套房"),
    PRESIDENTIAL_SUITE("总统套房");

    private final String displayName;
}
