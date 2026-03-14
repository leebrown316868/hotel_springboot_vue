package com.hotel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GuestStatus {
    VIP("VIP"),
    ACTIVE("活跃"),
    INACTIVE("不活跃");

    private final String displayName;
}
