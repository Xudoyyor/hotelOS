package com.hotelos.receptionservice.repositories;

import com.hotelos.receptionservice.entities.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {

}
