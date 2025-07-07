package com.eaglebank.repository;

import com.eaglebank.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    boolean existsByUserId(UUID userId);
    List<BankAccount> findByUserId(UUID userId);
    Optional<BankAccount> findByIdAndUserId(UUID accountId, UUID userId);

}

