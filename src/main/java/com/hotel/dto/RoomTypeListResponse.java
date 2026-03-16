package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomTypeListResponse {

    private List<RoomTypeResponse> roomTypes;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}
