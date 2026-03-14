package com.hotel.service;

import com.hotel.dto.BookingSummary;
import com.hotel.dto.GuestListResponse;
import com.hotel.dto.GuestRequest;
import com.hotel.dto.GuestResponse;

import java.util.List;

public interface GuestService {

    GuestListResponse getGuests(int page, int size, String searchQuery);

    GuestResponse getGuestById(Long id);

    GuestResponse createGuest(GuestRequest request);

    GuestResponse updateGuest(Long id, GuestRequest request);

    void deleteGuest(Long id);

    List<BookingSummary> getGuestBookings(Long id);
}
