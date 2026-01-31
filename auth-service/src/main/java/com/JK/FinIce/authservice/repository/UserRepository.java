package com.JK.FinIce.authservice.repository;

import com.JK.FinIce.authservice.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = ?1 OR u.email = ?1")
    Optional<User>  findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
