package com.hotel.mapper;

import com.hotel.dto.RoomListResponse;
import com.hotel.dto.RoomRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .number(room.getNumber())
                .floor(room.getFloor())
                .type(room.getType() != null ? room.getType().name() : null)
                .status(room.getStatus() != null ? room.getStatus().name() : null)
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public Room toEntity(RoomRequest request) {
        return Room.builder()
                .number(request.getNumber())
                .floor(request.getFloor())
                .type(parseRoomType(request.getType()))
                .status(parseRoomStatus(request.getStatus()))
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .build();
    }

    public RoomListResponse toResponseList(Page<Room> rooms) {
        List<RoomResponse> roomResponses = rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return RoomListResponse.builder()
                .rooms(roomResponses)
                .total(rooms.getTotalElements())
                .page(rooms.getNumber())
                .size(rooms.getSize())
                .totalPages(rooms.getTotalPages())
                .build();
    }

    public RoomType parseRoomType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        try {
            return RoomType.valueOf(type);
        } catch (IllegalArgumentException e) {
            for (RoomType roomType : RoomType.values()) {
                if (roomType.getDisplayName().equals(type)) {
                    return roomType;
                }
            }
            return null;
        }
    }

    private RoomStatus parseRoomStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return RoomStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            for (RoomStatus roomStatus : RoomStatus.values()) {
                if (roomStatus.getDisplayName().equals(status)) {
                    return roomStatus;
                }
            }
            return null;
        }
    }
}
