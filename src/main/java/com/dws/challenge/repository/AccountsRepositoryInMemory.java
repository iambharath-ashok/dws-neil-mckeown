package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the AccountsRepository interface.
 * This class manages account storage using a ConcurrentHashMap to ensure
 * thread-safe operations.
 */
@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    // Map to store accounts, using account ID as the key for fast retrieval.
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Creates a new account in the repository.
     *
     * @param account the account to be created.
     * @throws DuplicateAccountIdException if an account with the same ID already exists.
     */
    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        // Attempt to add the account. If an account with the same ID exists, an exception is thrown.
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    /**
     * Retrieves an account by its unique account ID.
     *
     * @param accountId the ID of the account to retrieve.
     * @return the Account object associated with the specified ID, or null if not found.
     */
    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    /**
     * Clears all accounts from the repository.
     * This method is typically used for resetting the repository state.
     */
    @Override
    public void clearAccounts() {
        accounts.clear();
    }
}
