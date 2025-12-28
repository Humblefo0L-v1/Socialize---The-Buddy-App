package com.socialize.user.repository;

import com.socialize.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.location WHERE u.id = :id")
    Optional<User> findByIdWithLocation(Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.location WHERE u.email = :email")
    Optional<User> findByEmailWithLocation(String email);
}