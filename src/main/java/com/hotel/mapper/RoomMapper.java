package com.hotel.mapper;

import com.hotel.dto.RoomListResponse;
import com.hotel.dto.RoomRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import com.hotel.entity.RoomTypeEntity;
import com.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final RoomTypeRepository roomTypeRepository;

    public RoomResponse toResponse(Room room) {
        // 从房型配置获取容量，而不是使用rooms表的冗余字段
        Integer capacity = room.getCapacity(); // 默认使用房间自己的容量
        if (room.getType() != null) {
            // 尝试从房型配置获取容量
            String typeCode = room.getType().name();
            capacity = roomTypeRepository.findByCode(typeCode)
                    .map(RoomTypeEntity::getCapacity)
                    .orElse(capacity); // 如果找不到房型配置，使用房间自己的容量
        }

        return RoomResponse.builder()
                .id(room.getId())
                .number(room.getNumber())
                .floor(room.getFloor())
                .type(room.getType() != null ? room.getType().name() : null)
                .status(room.getStatus() != null ? room.getStatus().name() : null)
                .price(room.getPrice())
                .capacity(capacity)
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
