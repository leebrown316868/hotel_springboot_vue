package com.hotel.service;

import com.hotel.dto.RoomListResponse;
import com.hotel.dto.RoomRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.dto.RoomTypeStats;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RoomService {

    RoomListResponse getRooms(int page, int size, String number, String floor, String status);

    RoomResponse getRoomById(Long id);

    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Long id, RoomRequest request);

    void deleteRoom(Long id);

    RoomResponse updateRoomStatus(Long id, String status);

    RoomTypeStats getRoomTypeStats(String roomType);

    String uploadRoomImage(Long roomId, MultipartFile file) throws IOException;

    void batchDeleteRooms(List<Long> ids);
}
