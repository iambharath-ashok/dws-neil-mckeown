package com.dws.challenge.exception;

/**
 * Exception thrown when an attempt is made to create or operate on
 * an account with an account ID that already exists in the system.
 */
public class DuplicateAccountIdException extends RuntimeException {

  /**
   * Constructs a new DuplicateAccountIdException with the specified detail message.
   *
   * @param message the detail message which is saved for later retrieval by the {@link Throwable#getMessage()} method.
   */
  public DuplicateAccountIdException(String message) {
    super(message);
  }
}
