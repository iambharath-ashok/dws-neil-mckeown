package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FundsTransferServiceTest {

  @Mock
  private AccountsRepository accountsRepository;

  @Mock
  private NotificationService notificationService; // Mocking NotificationService

  @InjectMocks
  private AccountsService accountsService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this); // Initialize the mocks
  }

  /**
   * Tests a successful transfer of funds between two accounts.
   */
  @Test
  void testSuccessfulTransfer() {
    Account accountFrom = new Account("1", BigDecimal.valueOf(100));
    Account accountTo = new Account("2", BigDecimal.valueOf(50));

    when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountsRepository.getAccount("2")).thenReturn(accountTo);

    accountsService.transferMoney("1", "2", BigDecimal.valueOf(50));

    // Verify balances after transfer
    assertEquals(BigDecimal.valueOf(50), accountFrom.getBalance());
    assertEquals(BigDecimal.valueOf(100), accountTo.getBalance());

    // Verify notifications
    verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 50 to account 2");
    verify(notificationService).notifyAboutTransfer(accountTo, "Received 50 from account 1");
  }

  /**
   * Tests a transfer attempt with insufficient funds in the source account.
   */
  @Test
  void testTransferInsufficientFunds() {
    Account accountFrom = new Account("1", BigDecimal.valueOf(30));
    Account accountTo = new Account("2", BigDecimal.valueOf(50));

    when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountsRepository.getAccount("2")).thenReturn(accountTo);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "2", BigDecimal.valueOf(50))
    );

    // Confirm the exception message
    assertEquals("Insufficient funds in account 1", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(30), accountFrom.getBalance());
    assertEquals(BigDecimal.valueOf(50), accountTo.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }

  /**
   * Tests that a transfer with a negative amount fails.
   */
  @Test
  void testTransferWithNegativeAmount() {
    Account accountFrom = new Account("1", BigDecimal.valueOf(100));
    Account accountTo = new Account("2", BigDecimal.valueOf(50));

    when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountsRepository.getAccount("2")).thenReturn(accountTo);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "2", BigDecimal.valueOf(-10))
    );

    // Confirm the exception message
    assertEquals("Transfer amount must be positive.", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(100), accountFrom.getBalance());
    assertEquals(BigDecimal.valueOf(50), accountTo.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }

  /**
   * Tests that a transfer of zero amount fails.
   */
  @Test
  void testTransferWithZeroAmount() {
    Account accountFrom = new Account("1", BigDecimal.valueOf(100));
    Account accountTo = new Account("2", BigDecimal.valueOf(50));

    when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountsRepository.getAccount("2")).thenReturn(accountTo);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "2", BigDecimal.ZERO)
    );

    // Confirm the exception message
    assertEquals("Transfer amount must be positive.", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(100), accountFrom.getBalance());
    assertEquals(BigDecimal.valueOf(50), accountTo.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }

  /**
   * Tests a transfer attempt between the same account.
   */
  @Test
  void testTransferBetweenSameAccount() {
    Account account = new Account("1", BigDecimal.valueOf(100));

    when(accountsRepository.getAccount("1")).thenReturn(account);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "1", BigDecimal.valueOf(50))
    );

    // Confirm the exception message
    assertEquals("Cannot transfer to the same account.", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(100), account.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }

  /**
   * Tests a transfer attempt from a non-existent source account.
   */
  @Test
  void testTransferFromNonExistentAccount() {
    Account accountTo = new Account("2", BigDecimal.valueOf(50));

    when(accountsRepository.getAccount("1")).thenReturn(null);
    when(accountsRepository.getAccount("2")).thenReturn(accountTo);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "2", BigDecimal.valueOf(50))
    );

    // Confirm the exception message
    assertEquals("Account not found: 1", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(50), accountTo.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }

  /**
   * Tests a transfer attempt to a non-existent destination account.
   */
  @Test
  void testTransferToNonExistentAccount() {
    Account accountFrom = new Account("1", BigDecimal.valueOf(100));

    when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountsRepository.getAccount("2")).thenReturn(null);

    Exception exception = assertThrows(IllegalArgumentException.class, () ->
            accountsService.transferMoney("1", "2", BigDecimal.valueOf(50))
    );

    // Confirm the exception message
    assertEquals("Account not found: 2", exception.getMessage());

    // Ensure no balance changes or notifications occurred
    assertEquals(BigDecimal.valueOf(100), accountFrom.getBalance());
    verify(notificationService, never()).notifyAboutTransfer(any(), any());
  }
}
