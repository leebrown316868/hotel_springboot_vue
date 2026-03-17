package com.hotel.service.impl;

import com.hotel.dto.RoomListResponse;
import com.hotel.dto.RoomRequest;
import com.hotel.dto.RoomResponse;
import com.hotel.dto.RoomTypeStats;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

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

    @Override
    public RoomTypeStats getRoomTypeStats(String roomType) {
        // roomType is the enum name (SINGLE, DOUBLE, etc.) which is stored in database
        List<Room> rooms = roomRepository.findByTypeName(roomType);
        RoomTypeStats stats = new RoomTypeStats();
        stats.setCode(roomType);
        stats.setRoomCount(rooms.size());
        stats.setAvailableCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        stats.setOccupiedCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        stats.setCleaningCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count());
        stats.setMaintenanceCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count());
        return stats;
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

    @Override
    public String uploadRoomImage(Long roomId, MultipartFile file) throws IOException {
        // 验证房间是否存在
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("房间不存在: " + roomId));

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 验证文件大小（5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过5MB");
        }

        // 创建房间目录
        String roomDirPath = uploadDir + File.separator + "rooms" + File.separator + roomId;
        File roomDir = new File(roomDirPath);
        if (!roomDir.exists()) {
            roomDir.mkdirs();
        }

        // 生成文件名：时间戳_随机字符.扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 保存文件
        Path filePath = Paths.get(roomDirPath, filename);
        Files.write(filePath, file.getBytes());

        // 返回访问URL
        return "/uploads/rooms/" + roomId + "/" + filename;
    }
}
