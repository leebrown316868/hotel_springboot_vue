package com.hotel.mapper;

import com.hotel.dto.BookingSummary;
import com.hotel.dto.GuestListResponse;
import com.hotel.dto.GuestRequest;
import com.hotel.dto.GuestResponse;
import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GuestMapper {

    public GuestResponse toResponse(Guest guest) {
        return GuestResponse.builder()
                .id(guest.getId())
                .name(guest.getName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .country(guest.getCountry())
                .status(guest.getStatus() != null ? guest.getStatus().getDisplayName() : null)
                .totalBookings(guest.getTotalBookings())
                .lastStay(guest.getLastStay())
                .createdAt(guest.getCreatedAt())
                .updatedAt(guest.getUpdatedAt())
                .build();
    }

    public Guest toEntity(GuestRequest request) {
        return Guest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .country(request.getCountry())
                .status(parseGuestStatus(request.getStatus()))
                .lastStay(request.getLastStay())
                .build();
    }

    public GuestListResponse toResponseList(Page<Guest> guests) {
        List<GuestResponse> guestResponses = guests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return GuestListResponse.builder()
                .guests(guestResponses)
                .total(guests.getTotalElements())
                .page(guests.getNumber())
                .size(guests.getSize())
                .totalPages(guests.getTotalPages())
                .build();
    }

    // Note: Booking entity will be implemented in Phase 4
    // This method will be implemented when Booking entity is available
    public BookingSummary toBookingSummary(Object booking) {
        // TODO: Implement when Booking entity is created in Phase 4
        return null;
    }

    public GuestStatus parseGuestStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return GuestStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            for (GuestStatus guestStatus : GuestStatus.values()) {
                if (guestStatus.getDisplayName().equals(status)) {
                    return guestStatus;
                }
            }
            return null;
        }
    }
}
