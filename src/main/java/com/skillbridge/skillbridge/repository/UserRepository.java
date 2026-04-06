package com.skillbridge.skillbridge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillbridge.skillbridge.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}