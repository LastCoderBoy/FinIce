package com.jk.finice.accountservice.repository;

import com.jk.finice.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByIban(String iban);

    Optional<Account> findByIban(String iban);

    @Query("SELECT COUNT(acc) FROM Account acc " +
            "WHERE acc.userId = :userId AND acc.accountType = 'CURRENT' AND acc.status = 'ACTIVE' ")
    int countCurrentAccountTypeForUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(acc) FROM Account acc " +
            "WHERE acc.userId = :userId AND acc.accountType = 'SAVINGS' AND acc.status = 'ACTIVE'")
    int countSavingsAccountTypeForUserId(@Param("userId") Long userId);

    // Find all accounts for user
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<Account> findActiveAccountsByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long userId, Long accountId);
}
