package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerConcurrencyTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
    accountsService.getAccountsRepository().clearAccounts();

    // Set up multiple accounts for concurrency tests
    accountsService.createAccount(new Account("Id-1", new BigDecimal("1000")));
    accountsService.createAccount(new Account("Id-2", new BigDecimal("1000")));
    accountsService.createAccount(new Account("Id-3", new BigDecimal("1000")));
    accountsService.createAccount(new Account("Id-4", new BigDecimal("1000")));
  }

  /**
   * Test concurrent transfers between multiple accounts.
   * 
   * Scenario:
   * - This test simulates multiple transfers occurring simultaneously to verify that
   *   the transferMoney method is thread-safe and can handle concurrent transfers without issues.
   * - Four concurrent transfers between four accounts are performed.
   * 
   * Expected Outcome:
   * - All transfers should complete successfully with an HTTP status of 200 OK.
   * - The total balance across all accounts should remain consistent at 4000.
   */
  @RepeatedTest(5)
  void testConcurrentTransfers() throws InterruptedException, ExecutionException {
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    
    // Create several concurrent transfer tasks
    futures.add(CompletableFuture.runAsync(() -> {
      try {
        this.mockMvc.perform(post("/v1/accounts/transfer")
            .param("accountFromId", "Id-1")
            .param("accountToId", "Id-2")
            .param("amount", "100"))
            .andExpect(status().isOk());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));

    futures.add(CompletableFuture.runAsync(() -> {
      try {
        this.mockMvc.perform(post("/v1/accounts/transfer")
            .param("accountFromId", "Id-2")
            .param("accountToId", "Id-3")
            .param("amount", "200"))
            .andExpect(status().isOk());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));

    futures.add(CompletableFuture.runAsync(() -> {
      try {
        this.mockMvc.perform(post("/v1/accounts/transfer")
            .param("accountFromId", "Id-3")
            .param("accountToId", "Id-4")
            .param("amount", "300"))
            .andExpect(status().isOk());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));

    futures.add(CompletableFuture.runAsync(() -> {
      try {
        this.mockMvc.perform(post("/v1/accounts/transfer")
            .param("accountFromId", "Id-4")
            .param("accountToId", "Id-1")
            .param("amount", "400"))
            .andExpect(status().isOk());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));

    // Wait for all tasks to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

    // Validate account balances after concurrent transfers
    Account account1 = accountsService.getAccount("Id-1");
    Account account2 = accountsService.getAccount("Id-2");
    Account account3 = accountsService.getAccount("Id-3");
    Account account4 = accountsService.getAccount("Id-4");

    // Total funds should remain the same (4000 across all accounts)
    BigDecimal totalBalance = account1.getBalance()
                                       .add(account2.getBalance())
                                       .add(account3.getBalance())
                                       .add(account4.getBalance());

    assertThat(totalBalance).isEqualByComparingTo("4000");
  }

  /**
   * Test high volume of concurrent transfers.
   * 
   * Scenario:
   * - Simulate 100 concurrent transfers across four accounts to ensure that the method 
   *   is deadlock-free and maintains data consistency during high concurrency.
   * - This test uses four accounts and alternates the "from" and "to" accounts 
   *   to increase concurrency.
   * 
   * Expected Outcome:
   * - All transfers should complete successfully with an HTTP status of 200 OK.
   * - The total balance across all accounts should remain consistent at 4000.
   */
  @Test
  void testHighVolumeConcurrentTransfers() throws InterruptedException, ExecutionException {
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // Run 100 concurrent transfers to verify deadlock prevention and consistency
    for (int i = 0; i < 100; i++) {
      String fromAccount = (i % 4 == 0) ? "Id-1" : (i % 4 == 1) ? "Id-2" : (i % 4 == 2) ? "Id-3" : "Id-4";
      String toAccount = (i % 4 == 0) ? "Id-2" : (i % 4 == 1) ? "Id-3" : (i % 4 == 2) ? "Id-4" : "Id-1";

      futures.add(CompletableFuture.runAsync(() -> {
        try {
          this.mockMvc.perform(post("/v1/accounts/transfer")
              .param("accountFromId", fromAccount)
              .param("accountToId", toAccount)
              .param("amount", "10"))
              .andExpect(status().isOk());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }));
    }

    // Wait for all transfers to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

    // Ensure the total balance remains consistent after high volume concurrent transfers
    Account account1 = accountsService.getAccount("Id-1");
    Account account2 = accountsService.getAccount("Id-2");
    Account account3 = accountsService.getAccount("Id-3");
    Account account4 = accountsService.getAccount("Id-4");

    BigDecimal totalBalance = account1.getBalance()
                                       .add(account2.getBalance())
                                       .add(account3.getBalance())
                                       .add(account4.getBalance());

    assertThat(totalBalance).isEqualByComparingTo("4000");
  }
}
