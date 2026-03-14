package com.hotel.specification;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<Room> withFilters(String number, String floor, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(number)) {
                predicates.add(criteriaBuilder.like(
                        root.get("number"),
                        "%" + number + "%"
                ));
            }

            if (StringUtils.isNotBlank(floor)) {
                predicates.add(criteriaBuilder.equal(
                        root.get("floor"),
                        floor
                ));
            }

            if (StringUtils.isNotBlank(status)) {
                RoomStatus roomStatus = parseRoomStatus(status);
                if (roomStatus != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("status"),
                            roomStatus
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static RoomStatus parseRoomStatus(String status) {
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
