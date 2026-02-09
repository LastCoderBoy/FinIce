package com.JK.FinIce.authservice.repository;

import com.JK.FinIce.authservice.entity.EmailToken;
import com.JK.FinIce.authservice.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM EmailToken et WHERE et.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailToken et SET et.usedAt = CURRENT_TIMESTAMP " +
            "WHERE et.user.id = :userId AND et.tokenType = :tokenType AND et.usedAt IS NULL")
    int revokeUnusedTokensByUserAndType(Long userId, TokenType tokenType);


    Optional<EmailToken> findByTokenAndTokenType(String token, TokenType tokenType);
}
