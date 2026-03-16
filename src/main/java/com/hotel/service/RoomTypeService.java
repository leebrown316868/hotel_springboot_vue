package com.hotel.service;

import com.hotel.dto.RoomTypeListResponse;
import com.hotel.dto.RoomTypeRequest;
import com.hotel.dto.RoomTypeResponse;
import com.hotel.entity.RoomTypeEntity;

import java.util.List;

public interface RoomTypeService {

    RoomTypeListResponse getRoomTypes(int page, int size, String search);

    RoomTypeResponse getRoomTypeById(Long id);

    List<RoomTypeResponse> getAllActiveRoomTypes();

    RoomTypeResponse createRoomType(RoomTypeRequest request);

    RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request);

    void deleteRoomType(Long id);
}
