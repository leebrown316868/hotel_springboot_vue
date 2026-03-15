package com.hotel.service.impl;

import com.hotel.dto.RoomListResponse;
import com.hotel.dto.RoomRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.exception.InvalidRoomStatusException;
import com.hotel.exception.RoomAlreadyExistsException;
import com.hotel.exception.RoomNotFoundException;
import com.hotel.mapper.RoomMapper;
import com.hotel.repository.RoomRepository;
import com.hotel.service.RoomService;
import com.hotel.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    public RoomListResponse getRooms(int page, int size, String number, String floor, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("number").ascending());
        Page<Room> rooms = roomRepository.findAll(
                RoomSpecification.withFilters(number, floor, status),
                pageable
        );
        return roomMapper.toResponseList(rooms);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + id));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByNumber(request.getNumber())) {
            throw new RoomAlreadyExistsException("房间号已存在: " + request.getNumber());
        }

        Room room = roomMapper.toEntity(request);
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }

        room = roomRepository.save(room);
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + id));

        if (!room.getNumber().equals(request.getNumber()) &&
                roomRepository.existsByNumber(request.getNumber())) {
            throw new RoomAlreadyExistsException("房间号已存在: " + request.getNumber());
        }

        room.setNumber(request.getNumber());
        room.setFloor(request.getFloor());
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            room.setType(roomMapper.parseRoomType(request.getType()));
        }
        room.setPrice(request.getPrice());

        room = roomRepository.save(room);
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + id));

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new InvalidRoomStatusException("已入住的房间不能删除");
        }

        roomRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RoomResponse updateRoomStatus(Long id, String status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + id));

        RoomStatus newStatus = parseRoomStatus(status);
        if (newStatus == null) {
            throw new InvalidRoomStatusException("无效的房间状态: " + status);
        }

        room.setStatus(newStatus);
        room = roomRepository.save(room);
        return roomMapper.toResponse(room);
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
