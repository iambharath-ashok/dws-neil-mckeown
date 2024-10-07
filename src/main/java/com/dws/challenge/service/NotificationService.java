package com.dws.challenge.service;

import com.dws.challenge.domain.Account;

/**
 * Interface for sending notifications related to account events.
 * This service can be implemented to notify account holders about
 * various activities, such as fund transfers.
 */
public interface NotificationService {

  /**
   * Sends a notification about a transfer to the specified account owner.
   *
   * @param account              the account associated with the transfer notification.
   * @param transferDescription  a description of the transfer event to be communicated.
   */
  void notifyAboutTransfer(Account account, String transferDescription);
}
