package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Service class for managing account operations, including account creation,
 * retrieval, and money transfers between accounts.
 */
@Service
@Slf4j
@Validated
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;

  /**
   * Constructs an AccountsService with the specified repository and notification service.
   *
   * @param accountsRepository the repository for account data management.
   * @param notificationService the service for sending notifications.
   */
  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  /**
   * Creates a new account in the repository.
   *
   * @param account The account to be created.
   */
  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  /**
   * Retrieves an account by its unique ID.
   *
   * @param accountId The ID of the account to retrieve.
   * @return The requested account, or null if it does not exist.
   */
  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  /**
   * Transfers a specified amount from one account to another.
   *
   * @param accountFromId The ID of the account to transfer funds from.
   * @param accountToId   The ID of the account to transfer funds to.
   * @param amount        The amount to transfer (must be positive).
   *
   * @throws IllegalArgumentException if any parameter is invalid or if there are insufficient funds.
   * @throws IllegalStateException if unable to acquire necessary locks within the time limit.
   */
  public void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
    // Validate that the transfer amount is positive
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive.");
    }

    // Ensure source and destination accounts are not the same
    if (accountFromId.equals(accountToId)) {
      throw new IllegalArgumentException("Cannot transfer to the same account.");
    }

    // Retrieve both accounts and check if they exist
    Account accountFrom = this.accountsRepository.getAccount(accountFromId);
    Account accountTo = this.accountsRepository.getAccount(accountToId);

    if (accountFrom == null) {
      throw new IllegalArgumentException("Account not found: " + accountFromId);
    }
    if (accountTo == null) {
      throw new IllegalArgumentException("Account not found: " + accountToId);
    }

    // Ensure consistent lock order to avoid deadlocks
    Account firstLock = accountFromId.compareTo(accountToId) < 0 ? accountFrom : accountTo;
    Account secondLock = accountFromId.compareTo(accountToId) < 0 ? accountTo : accountFrom;

    try {
      // Attempt to acquire the first lock within 1 second
      if (firstLock.getLock().tryLock(1, TimeUnit.SECONDS)) {
        try {
          // Attempt to acquire the second lock within 1 second
          if (secondLock.getLock().tryLock(1, TimeUnit.SECONDS)) {
            try {
              // Check if there are enough funds in the source account
              if (accountFrom.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds in account " + accountFromId);
              }

              // Update balances: subtract from source and add to destination
              accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
              accountTo.setBalance(accountTo.getBalance().add(amount));

              // Send notifications to both account holders
              notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to account " + accountToId);
              notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from account " + accountFromId);

              // Log the completed transfer
              log.info("Transfer completed from account {} to account {} for amount {}", accountFromId, accountToId, amount);
            } finally {
              // Always unlock the second lock
              secondLock.getLock().unlock();
            }
          } else {
            // Handle case where the second lock could not be acquired
            throw new IllegalStateException("Could not acquire lock on account " + accountToId);
          }
        } finally {
          // Always unlock the first lock
          firstLock.getLock().unlock();
        }
      } else {
        // Handle case where the first lock could not be acquired
        throw new IllegalStateException("Could not acquire lock on account " + accountFromId);
      }
    } catch (InterruptedException e) {
      // Handle the interrupt and restore the thread's interrupt status
      Thread.currentThread().interrupt();
      throw new RuntimeException("Transfer interrupted", e);
    }
  }
}
