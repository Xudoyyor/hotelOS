package com.hotelos.receptionservice.repositories;

import com.hotelos.receptionservice.entities.BillingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillingItemRepository extends JpaRepository<BillingItem,Long> {
    List<BillingItem> findByBookingId(UUID bookingId);
}
