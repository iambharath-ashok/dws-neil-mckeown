package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;

/* Changed import from javax.validation to jakarta for compatibility with Jakarta EE 9 and later. */
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class Account {

  // Unique identifier for the account, must not be null or empty.
  @NotNull
  @NotEmpty
  private final String accountId;

  // Balance of the account, must not be null and must be at least zero to ensure it is a valid initial balance.
  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  /*
   * Lock used to handle concurrent transfers safely.
   * A ReentrantLock is employed to ensure thread safety and prevent deadlocks during balance modifications.
   * The lock is annotated with @JsonIgnore to prevent serialization and deserialization issues.
   */
  @JsonIgnore
  private final ReentrantLock lock = new ReentrantLock();

  // Constructor to create an Account instance with a specified account ID and initialize balance to zero.
  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  // Constructor to create an Account instance with a specified account ID and initial balance.
  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
                 @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
}
