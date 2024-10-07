package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

/**
 * Interface for managing account-related operations in a repository.
 * This interface defines methods for creating, retrieving, and clearing accounts.
 */
public interface AccountsRepository {

  /**
   * Creates a new account in the repository.
   *
   * @param account the account to be created.
   * @throws DuplicateAccountIdException if an account with the same ID already exists.
   */
  void createAccount(Account account) throws DuplicateAccountIdException;

  /**
   * Retrieves an account by its unique account ID.
   *
   * @param accountId the ID of the account to retrieve.
   * @return the Account object associated with the specified ID, or null if not found.
   */
  Account getAccount(String accountId);

  /**
   * Clears all accounts from the repository.
   * This method is typically used for resetting the repository state.
   */
  void clearAccounts();
}
