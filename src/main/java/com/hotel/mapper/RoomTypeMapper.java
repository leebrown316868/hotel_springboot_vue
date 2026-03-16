package com.hotel.mapper;

import com.hotel.dto.RoomTypeListResponse;
import com.hotel.dto.RoomTypeRequest;
import com.hotel.dto.RoomTypeResponse;
import com.hotel.entity.RoomTypeEntity;
import com.hotel.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoomTypeMapper {

    private final RoomRepository roomRepository;

    public RoomTypeMapper(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomTypeResponse toResponse(RoomTypeEntity entity) {
        long roomCount = roomRepository.countByType(entity.getCode());

        return RoomTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .basePrice(entity.getBasePrice())
                .active(entity.getActive())
                .roomCount((int) roomCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RoomTypeEntity toEntity(RoomTypeRequest request) {
        return RoomTypeEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacity(request.getCapacity())
                .basePrice(request.getBasePrice())
                .active(true)
                .build();
    }

    public RoomTypeListResponse toResponseList(Page<RoomTypeEntity> roomTypes) {
        List<RoomTypeResponse> roomTypeResponses = roomTypes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return RoomTypeListResponse.builder()
                .roomTypes(roomTypeResponses)
                .total(roomTypes.getTotalElements())
                .page(roomTypes.getNumber())
                .size(roomTypes.getSize())
                .totalPages(roomTypes.getTotalPages())
                .build();
    }
}
