package com.hotel.service.impl;

import com.hotel.dto.RoomTypeListResponse;
import com.hotel.dto.RoomTypeRequest;
import com.hotel.dto.RoomTypeResponse;
import com.hotel.entity.RoomTypeEntity;
import com.hotel.exception.RoomTypeAlreadyExistsException;
import com.hotel.exception.RoomTypeInUseException;
import com.hotel.exception.RoomTypeNotFoundException;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.service.RoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeMapper roomTypeMapper;

    public RoomTypeServiceImpl(
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            RoomTypeMapper roomTypeMapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.roomTypeMapper = roomTypeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeListResponse getRoomTypes(int page, int size, String search) {
        log.info("Fetching room types - page: {}, size: {}, search: {}", page, size, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("code").ascending());
        Page<RoomTypeEntity> roomTypes;

        if (search != null && !search.trim().isEmpty()) {
            roomTypes = roomTypeRepository.searchRoomTypes(search, pageable);
        } else {
            roomTypes = roomTypeRepository.findAll(pageable);
        }

        return roomTypeMapper.toResponseList(roomTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(Long id) {
        log.info("Fetching room type by id: {}", id);

        RoomTypeEntity roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        return roomTypeMapper.toResponse(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getAllActiveRoomTypes() {
        log.info("Fetching all active room types");

        return roomTypeRepository.findByActiveTrueOrderByCodeAsc()
                .stream()
                .map(roomTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        log.info("Creating room type with code: {}", request.getCode());

        // Check if code already exists
        if (roomTypeRepository.existsByCode(request.getCode())) {
            throw new RoomTypeAlreadyExistsException(request.getCode());
        }

        RoomTypeEntity roomType = roomTypeMapper.toEntity(request);
        RoomTypeEntity savedRoomType = roomTypeRepository.save(roomType);

        log.info("Room type created successfully with id: {}", savedRoomType.getId());
        return roomTypeMapper.toResponse(savedRoomType);
    }

    @Override
    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        log.info("Updating room type with id: {}", id);

        RoomTypeEntity roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        // 保存旧的房型代码和价格，用于后续同步
        String oldCode = roomType.getCode();
        java.math.BigDecimal oldPrice = roomType.getBasePrice();

        // Check if code already exists for another room type
        if (roomTypeRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RoomTypeAlreadyExistsException(request.getCode());
        }

        roomType.setCode(request.getCode());
        roomType.setName(request.getName());
        roomType.setCapacity(request.getCapacity());
        roomType.setBasePrice(request.getBasePrice());

        RoomTypeEntity updatedRoomType = roomTypeRepository.save(roomType);

        // 如果价格发生了变化，同步更新所有关联房间的价格
        java.math.BigDecimal newPrice = request.getBasePrice();
        if (newPrice != null && oldPrice != null && oldPrice.compareTo(newPrice) != 0) {
            log.info("Price changed from {} to {}, updating all rooms of type {}",
                    oldPrice, newPrice, updatedRoomType.getCode());
            roomRepository.updatePriceByType(updatedRoomType.getCode(), newPrice);
            log.info("Updated prices for all rooms of type {}", updatedRoomType.getCode());
        }

        log.info("Room type updated successfully: {}", updatedRoomType.getId());
        return roomTypeMapper.toResponse(updatedRoomType);
    }

    @Override
    public void deleteRoomType(Long id) {
        log.info("Deleting room type with id: {}", id);

        RoomTypeEntity roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        // Check if room type is in use
        long roomCount = roomRepository.countByType(roomType.getCode());
        if (roomCount > 0) {
            throw new RoomTypeInUseException(roomType.getName());
        }

        roomTypeRepository.delete(roomType);

        log.info("Room type deleted successfully: {}", id);
    }
}
