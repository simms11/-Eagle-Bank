package com.eaglebank.repository;

import com.eaglebank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);

    List<Transaction> findByFromAccountIdInOrToAccountIdIn(List<UUID> fromAccountIds, List<UUID> toAccountIds);

}
