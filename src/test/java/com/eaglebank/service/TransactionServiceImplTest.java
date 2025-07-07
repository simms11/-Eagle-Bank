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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private BankAccount senderAccount;
    private BankAccount receiverAccount;
    private Transaction transaction;
    private String senderEmail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transactionId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        senderEmail = "alice@example.com";

        senderAccount = BankAccount.builder().id(senderId).balance(BigDecimal.valueOf(1000))
                .user(User.builder().id(senderId).email(senderEmail).build()).build();
        receiverAccount = BankAccount.builder().id(receiverId).balance(BigDecimal.valueOf(500))
                .user(User.builder().id(receiverId).email("bob@example.com").build()).build();

        transaction = Transaction.builder()
                .id(transactionId)
                .fromAccount(senderAccount)
                .toAccount(receiverAccount)
                .amount(BigDecimal.valueOf(100))
                .createdTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetTransactionById_Success_WhenUserIsSender() {
        when(authentication.getName()).thenReturn(senderEmail);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(User.builder().id(senderId).email(senderEmail).build()));

        TransactionResponse result = transactionService.getTransactionById(transactionId, authentication);

        assertNotNull(result);
        assertEquals(transactionId.toString(), result.transactionId());
        assertEquals(senderId.toString(), result.fromAccountId());
    }

    @Test
    void testGetTransactionById_Success_WhenUserIsRecipient() {
        UUID transactionId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        String recipientEmail = "recipient@example.com";

        // Create and link recipient User
        User recipient = new User();
        recipient.setId(recipientUserId);
        recipient.setEmail(recipientEmail);

        // Link user to 'to' account
        BankAccount toAccount = new BankAccount();
        toAccount.setId(UUID.randomUUID());
        toAccount.setUser(recipient);

        // Create dummy sender with user
        User sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setEmail("sender@example.com");

        BankAccount fromAccount = new BankAccount();
        fromAccount.setId(UUID.randomUUID());
        fromAccount.setUser(sender);

        // Transaction
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(new BigDecimal("100.00"))
                .createdTimestamp(LocalDateTime.now())
                .build();

        // Mocks
        when(authentication.getName()).thenReturn(recipientEmail);
        when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        TransactionResponse result = transactionService.getTransactionById(transactionId, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(transactionId.toString(), result.transactionId());
    }

    @Test
    void testGetTransactionById_ThrowsForbiddenException() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        String unknownEmail = "unauthorized@example.com";
        UUID unknownUserId = UUID.randomUUID();

        // Mock from-account user (authorized user)
        User fromUser = new User();
        fromUser.setId(UUID.randomUUID());

        BankAccount fromAccount = new BankAccount();
        fromAccount.setId(UUID.randomUUID());
        fromAccount.setUser(fromUser);

        // Mock to-account user (also authorized user)
        User toUser = new User();
        toUser.setId(UUID.randomUUID());

        BankAccount toAccount = new BankAccount();
        toAccount.setId(UUID.randomUUID());
        toAccount.setUser(toUser);

        Transaction txn = Transaction.builder()
                .id(transactionId)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(new BigDecimal("100"))
                .createdTimestamp(LocalDateTime.now())
                .build();

        // Mock auth & repo
        when(authentication.getName()).thenReturn(unknownEmail);
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.of(
                User.builder().id(unknownUserId).email(unknownEmail).build()
        ));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(txn));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> transactionService.getTransactionById(transactionId, authentication));
    }

    @Test
    void testGetTransactionById_ThrowsNotFoundException_WhenTransactionMissing() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(transactionId, authentication));
    }

    @Test
    void testGetTransactions_success() {
        // Arrange
        String email = "john.doe@example.com";
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).email(email).build();
        BankAccount account1 = BankAccount.builder().id(UUID.randomUUID()).user(user).build();
        BankAccount account2 = BankAccount.builder().id(UUID.randomUUID()).user(user).build();

        Transaction txn1 = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccount(account1)
                .toAccount(account2)
                .amount(BigDecimal.valueOf(200.00))
                .createdTimestamp(LocalDateTime.now())
                .build();

        Transaction txn2 = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccount(account2)
                .toAccount(account1)
                .amount(BigDecimal.valueOf(100.00))
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findByUserId(userId)).thenReturn(List.of(account1, account2));
        when(transactionRepository.findByFromAccountIdInOrToAccountIdIn(anyList(), anyList()))
                .thenReturn(List.of(txn1, txn2));

        // Act
        List<TransactionResponse> result = transactionService.getTransactions(authentication);

        // Assert: check that both amounts exist, regardless of order
        List<BigDecimal> amounts = result.stream()
                .map(TransactionResponse::amount)
                .toList();

        assertTrue(amounts.contains(BigDecimal.valueOf(200.00)));
        assertTrue(amounts.contains(BigDecimal.valueOf(100.00)));
        assertEquals(2, result.size());
    }

    @Test
    void testGetTransactionsForAccount_success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "john.doe@example.com";

        User user = User.builder().id(userId).email(email).build();
        BankAccount account = BankAccount.builder().id(accountId).user(user).build();

        Transaction txn1 = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccount(account)
                .toAccount(account)
                .amount(BigDecimal.valueOf(300.00))
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId))
                .thenReturn(List.of(txn1));

        // Act
        List<TransactionResponse> result = transactionService.getTransactionsForAccount(accountId, authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).amount());
    }

    @Test
    void testGetTransactionsForAccount_forbidden() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        String authEmail = "user@example.com";
        String otherEmail = "other@example.com";

        User authUser = User.builder().id(UUID.randomUUID()).email(authEmail).build();
        User otherUser = User.builder().id(UUID.randomUUID()).email(otherEmail).build();
        BankAccount otherAccount = BankAccount.builder().id(accountId).user(otherUser).build();

        when(authentication.getName()).thenReturn(authEmail);
        when(userRepository.findByEmail(authEmail)).thenReturn(Optional.of(authUser));
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(otherAccount));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> transactionService.getTransactionsForAccount(accountId, authentication));
    }

    @Test
    void testCreateTransaction_Success() {
        // Arrange
        String email = "john.doe@example.com";
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(150.00);

        BankAccount from = BankAccount.builder()
                .id(fromId)
                .balance(BigDecimal.valueOf(300.00))
                .user(User.builder().email(email).build())
                .build();

        BankAccount to = BankAccount.builder()
                .id(toId)
                .balance(BigDecimal.valueOf(50.00))
                .user(User.builder().email("recipient@example.com").build())
                .build();

        CreateTransactionRequest request = new CreateTransactionRequest(
                fromId.toString(), toId.toString(), amount
        );

        when(authentication.getName()).thenReturn(email);
        when(bankAccountRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(bankAccountRepository.findById(toId)).thenReturn(Optional.of(to));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(UUID.randomUUID());
            return txn;
        });

        // Act
        TransactionResponse response = transactionService.createTransaction(request, authentication);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(amount);
        assertThat(from.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(to.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(200.00));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById_ThrowsResourceNotFound() {
        UUID transactionId = UUID.randomUUID();
        String email = "emily.johnson@example.com";

        when(authentication.getName()).thenReturn(email);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(transactionId, authentication));
    }

    @Test
    void testGetTransactionsForAccount_Success() {
        UUID accountId = UUID.randomUUID();
        String authEmail = "alice@example.com";
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email(authEmail).build();

        BankAccount ownedAccount = BankAccount.builder().id(accountId).user(user).build();

        Transaction txn1 = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccount(ownedAccount)
                .toAccount(BankAccount.builder().id(UUID.randomUUID()).user(user).build())
                .amount(BigDecimal.valueOf(50))
                .createdTimestamp(LocalDateTime.now())
                .build();

        Transaction txn2 = Transaction.builder()
                .id(UUID.randomUUID())
                .fromAccount(BankAccount.builder().id(UUID.randomUUID()).user(user).build())
                .toAccount(ownedAccount)
                .amount(BigDecimal.valueOf(80))
                .createdTimestamp(LocalDateTime.now())
                .build();

        when(authentication.getName()).thenReturn(authEmail);
        when(userRepository.findByEmail(authEmail)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(ownedAccount)); // ✅ Add this
        when(transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId))
                .thenReturn(List.of(txn1, txn2));

        List<TransactionResponse> responses = transactionService.getTransactionsForAccount(accountId, authentication);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(TransactionResponse::amount)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(50), BigDecimal.valueOf(80));
    }

    @Test
    void testGetTransactionsForAccount_ThrowsForbiddenException_WhenUserDoesNotOwnAccount() {
        UUID accountId = UUID.randomUUID();
        String authEmail = "eve@example.com";
        UUID authUserId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        User authUser = User.builder().id(authUserId).email(authEmail).build();

        // Account owned by someone else
        User otherUser = User.builder().id(differentUserId).email("owner@example.com").build();
        BankAccount account = BankAccount.builder().id(accountId).user(otherUser).build();

        when(authentication.getName()).thenReturn(authEmail);
        when(userRepository.findByEmail(authEmail)).thenReturn(Optional.of(authUser));
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(ForbiddenException.class, () ->
                transactionService.getTransactionsForAccount(accountId, authentication));
    }

    @Test
    void testGetTransactionsForAccount_ThrowsNotFoundException_WhenAccountMissing() {
        UUID accountId = UUID.randomUUID();
        String authEmail = "eve@example.com";
        UUID authUserId = UUID.randomUUID();

        User authUser = User.builder().id(authUserId).email(authEmail).build();

        when(authentication.getName()).thenReturn(authEmail);
        when(userRepository.findByEmail(authEmail)).thenReturn(Optional.of(authUser));
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty()); // Account not found

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.getTransactionsForAccount(accountId, authentication));
    }


}
