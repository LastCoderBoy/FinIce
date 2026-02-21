package com.jk.finice.accountservice.entity;

import com.jk.finice.accountservice.enums.CardStatus;
import com.jk.finice.accountservice.enums.CardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards",
        indexes = {
                @Index(name = "idx_card_number", columnList = "card_number"),
                @Index(name = "idx_card_account_id", columnList = "account_id"),
                @Index(name = "idx_card_status", columnList = "card_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_number", columnNames = "card_number")
        }

)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Card {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "card_number", nullable = false, unique = true, length = 16)
        private String cardNumber;

        @Column(name = "cvv", nullable = false, length = 3)
        private String cvv; // should be encrypted

        @Column(nullable = false, name = "expires_at")
        private LocalDate expiresAt;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, name = "card_status")
        @Builder.Default
        private CardStatus cardStatus = CardStatus.ACTIVE;

        @Enumerated(EnumType.STRING)
        @Column(name = "card_type", nullable = false, length = 20)
        private CardType cardType;  // DEBIT, CREDIT, VIRTUAL

        // ============= RELATIONSHIPS =============

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "account_id", nullable = false)
        private Account account;

        // ========== Metadata ==========

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Column(name = "last_used_at")
        private LocalDateTime lastUsedAt;

        // ========== Helper Methods ==========

        /**
         * Check if card is expired
         */
        public boolean isExpired() {
                return LocalDate.now().isAfter(this.expiresAt);
        }

        /**
         * Check if card is active
         */
        public boolean isActive() {
                return this.cardStatus == CardStatus.ACTIVE && !isExpired();
        }

        /**
         * Block the card
         */
        public void block() {
                this.cardStatus = CardStatus.BLOCKED;
        }

        /**
         * Activate the card
         */
        public void activate() {
                if (isExpired()) {
                        throw new IllegalStateException("Cannot activate expired card");
                }
                this.cardStatus = CardStatus.ACTIVE;
        }
}
