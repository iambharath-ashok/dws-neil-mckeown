package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementation of the NotificationService that sends email notifications
 * regarding account transfer events.
 */
@Slf4j
@Component
public class EmailNotificationService implements NotificationService {

  /**
   * Sends a notification about a transfer to the account owner.
   * <p>
   * This method is currently designed to log the notification action.
   * It is assumed that a colleague will implement the actual email sending functionality.
   * </p>
   *
   * @param account           the account for which the notification is sent.
   * @param transferDescription a description of the transfer event.
   */
  @Override
  public void notifyAboutTransfer(Account account, String transferDescription) {
    // THIS METHOD SHOULD NOT BE CHANGED - ASSUME YOUR COLLEAGUE WILL IMPLEMENT IT
    log.info("Sending notification to owner of {}: {}", account.getAccountId(), transferDescription);
  }
}
