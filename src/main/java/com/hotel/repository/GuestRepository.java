package com.hotel.repository;

import com.hotel.entity.Guest;
import com.hotel.entity.GuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {

    List<Guest> findByNameContainingIgnoreCase(String name);

    List<Guest> findByEmailContainingIgnoreCase(String email);

    Optional<Guest> findByEmail(String email);

    List<Guest> findByStatus(GuestStatus status);

    List<Guest> findByCountry(String country);

    boolean existsByEmail(String email);

    List<Guest> findByTotalBookingsGreaterThan(Integer count);
}
