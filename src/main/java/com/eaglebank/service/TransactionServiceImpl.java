package com.eaglebank.service;

import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.entity.BankAccount;
import com.eaglebank.entity.Transaction;
import com.eaglebank.entity.User;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Authentication auth) {
        UUID fromId = UUID.fromString(request.fromAccountId());
        UUID toId = UUID.fromString(request.toAccountId());
        BigDecimal amount = request.amount();

        BankAccount from = bankAccountRepository.findById(fromId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        BankAccount to = bankAccountRepository.findById(toId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient account not found"));

        // Only allow user to send from their own account
        String email = auth.getName();
        if (!from.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to send from this account");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Deduct & transfer funds
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction transaction = Transaction.builder()
                .fromAccount(from)
                .toAccount(to)
                .amount(amount)
                .createdTimestamp(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactions(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        List<UUID> accountIds = accounts.stream().map(BankAccount::getId).toList();

        List<Transaction> transactions = transactionRepository
                .findByFromAccountIdInOrToAccountIdIn(accountIds, accountIds);

        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedTimestamp).reversed())
                .map(this::toResponse)
                .toList();
    }


    @Override
    public TransactionResponse getTransactionById(UUID transactionId, Authentication auth) {
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        UUID userId = user.getId();
        boolean isSenderOrReceiver = txn.getFromAccount().getUser().getId().equals(userId) ||
                txn.getToAccount().getUser().getId().equals(userId);

        if (!isSenderOrReceiver) {
            throw new ForbiddenException("You are not allowed to view this transaction");
        }

        return toResponse(txn);
    }




    @Override
    public List<TransactionResponse> getTransactionsForAccount(UUID accountId, Authentication auth) {
        String email = auth.getName();

        // Verify user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify account exists and is owned by user
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this account");
        }

        // Fetch transactions
        List<Transaction> txns = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
        return txns.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedTimestamp).reversed())
                .map(this::toResponse)
                .toList();
    }


    private TransactionResponse toResponse(Transaction txn) {
        return new TransactionResponse(
                txn.getId().toString(),
                txn.getFromAccount().getId().toString(),
                txn.getToAccount().getId().toString(),
                txn.getAmount(),
                txn.getCreatedTimestamp()
        );
    }
}
