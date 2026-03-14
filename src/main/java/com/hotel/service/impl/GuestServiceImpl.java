package com.hotel.service.impl;

import com.hotel.dto.BookingSummary;
import com.hotel.dto.GuestListResponse;
import com.hotel.dto.GuestRequest;
import com.hotel.dto.GuestResponse;
import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import com.hotel.exception.GuestAlreadyExistsException;
import com.hotel.exception.GuestNotFoundException;
import com.hotel.mapper.GuestMapper;
import com.hotel.repository.GuestRepository;
import com.hotel.service.GuestService;
import com.hotel.specification.GuestSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    @Override
    public GuestListResponse getGuests(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Guest> guests = guestRepository.findAll(
                GuestSpecification.withSearchQuery(searchQuery),
                pageable
        );
        return guestMapper.toResponseList(guests);
    }

    @Override
    public GuestResponse getGuestById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + id));
        return guestMapper.toResponse(guest);
    }

    @Override
    @Transactional
    public GuestResponse createGuest(GuestRequest request) {
        if (guestRepository.existsByEmail(request.getEmail())) {
            throw new GuestAlreadyExistsException("邮箱已存在: " + request.getEmail());
        }

        Guest guest = guestMapper.toEntity(request);
        if (guest.getStatus() == null) {
            guest.setStatus(GuestStatus.ACTIVE);
        }

        guest = guestRepository.save(guest);
        log.info("Created guest with id: {}", guest.getId());
        return guestMapper.toResponse(guest);
    }

    @Override
    @Transactional
    public GuestResponse updateGuest(Long id, GuestRequest request) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + id));

        if (!guest.getEmail().equals(request.getEmail()) &&
                guestRepository.existsByEmail(request.getEmail())) {
            throw new GuestAlreadyExistsException("邮箱已存在: " + request.getEmail());
        }

        guest.setName(request.getName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setCountry(request.getCountry());

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            GuestStatus status = guestMapper.parseGuestStatus(request.getStatus());
            if (status != null) {
                guest.setStatus(status);
            }
        }

        if (request.getLastStay() != null) {
            guest.setLastStay(request.getLastStay());
        }

        guest = guestRepository.save(guest);
        log.info("Updated guest with id: {}", guest.getId());
        return guestMapper.toResponse(guest);
    }

    @Override
    @Transactional
    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new GuestNotFoundException("客户不存在: " + id));

        // TODO: 在Phase 4实现Booking后，可以添加检查：客户是否有未完成的预订
        // if (guest.getTotalBookings() > 0) {
        //     throw new GuestHasActiveBookingsException("客户有未完成的预订，不能删除");
        // }

        guestRepository.deleteById(id);
        log.info("Deleted guest with id: {}", id);
    }

    @Override
    public List<BookingSummary> getGuestBookings(Long id) {
        // 验证客户是否存在
        if (!guestRepository.existsById(id)) {
            throw new GuestNotFoundException("客户不存在: " + id);
        }

        // TODO: 在Phase 4实现Booking后，需要：
        // 1. 注入BookingRepository
        // 2. 使用 bookingRepository.findByGuestIdOrderByCreatedAtDesc(id)
        // 3. 使用 guestMapper.toBookingSummary() 转换每个Booking

        log.warn("getGuestBookings is not yet implemented - Booking entity will be created in Phase 4");
        return Collections.emptyList();
    }
}
