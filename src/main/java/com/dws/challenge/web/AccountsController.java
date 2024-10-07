package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;

/**
 * REST controller for managing account-related operations.
 * This class handles HTTP requests for creating accounts, retrieving accounts,
 * and transferring funds between accounts.
 */
@RestController
@RequestMapping("/v1/accounts")
@Slf4j
@Validated
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  /**
   * Creates a new account.
   *
   * @param account The account object to be created, provided in the request body.
   * @return ResponseEntity with HTTP status CREATED if successful, or BAD_REQUEST if the account ID already exists.
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
      // Attempt to create the account in the service layer
      this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      // Return a BAD_REQUEST response if the account ID is duplicate
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // If successful, return CREATED status
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Retrieves an account by its unique ID.
   *
   * @param accountId The ID of the account to retrieve.
   * @return The Account object if found, or null if not found.
   */
  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    // Fetches the account from the service layer based on accountId
    return this.accountsService.getAccount(accountId);
  }

  /**
   * Transfers money between two accounts.
   *
   * @param accountFromId The ID of the account from which the funds are being transferred.
   * @param accountToId   The ID of the account to which the funds are being transferred.
   * @param amount        The amount of money to transfer, which must be positive.
   * @return ResponseEntity with HTTP status OK if the transfer is successful,
   *         or BAD_REQUEST if any validation fails or business logic issues arise.
   */
  @PostMapping(path = "/transfer")
  public ResponseEntity<Object> transferMoney(
          @RequestParam String accountFromId,
          @RequestParam String accountToId,
          @RequestParam BigDecimal amount) {

    log.info("Transferring {} from account {} to account {}", amount, accountFromId, accountToId);

    try {
      // Attempt to transfer money using the service layer
      this.accountsService.transferMoney(accountFromId, accountToId, amount);
    } catch (IllegalArgumentException e) {
      // Return BAD_REQUEST if transfer fails due to validation or business logic errors
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // If successful, return OK status
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
