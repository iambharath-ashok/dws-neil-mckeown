package com.dws.challenge;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
    // Clear accounts and set up initial accounts for tests
    accountsService.getAccountsRepository().clearAccounts();
    accountsService.createAccount(new Account("Id-1", new BigDecimal("1000")));
    accountsService.createAccount(new Account("Id-2", new BigDecimal("500")));
  }

  /**
   * Tests successful creation of an account.
   */
  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  /**
   * Tests creating an account with a duplicate account ID.
   */
  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  /**
   * Tests account creation with no account ID.
   */
  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  /**
   * Tests account creation with no balance field.
   */
  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  /**
   * Tests account creation with no JSON body.
   */
  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  /**
   * Tests account creation with a negative balance.
   */
  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  /**
   * Tests account creation with an empty account ID.
   */
  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  /**
   * Tests successful retrieval of an existing account.
   */
  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
            .andExpect(status().isOk())
            .andExpect(
                    content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  /**
   * Tests a successful transfer of funds between accounts.
   */
  @Test
  void transferMoneySuccessful() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "Id-1")
                    .param("accountToId", "Id-2")
                    .param("amount", "200"))
            .andExpect(status().isOk());
  }

  /**
   * Tests a transfer with insufficient funds in the source account.
   */
  @Test
  void transferMoneyInsufficientFunds() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "Id-2")
                    .param("accountToId", "Id-1")
                    .param("amount", "600"))
            .andExpect(status().isBadRequest());
  }

  /**
   * Tests a transfer with a negative amount.
   */
  @Test
  void transferMoneyNegativeAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "Id-1")
                    .param("accountToId", "Id-2")
                    .param("amount", "-100"))
            .andExpect(status().isBadRequest());
  }

  /**
   * Tests a transfer to the same account (source and destination are identical).
   */
  @Test
  void transferMoneySameAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "Id-1")
                    .param("accountToId", "Id-1")
                    .param("amount", "100"))
            .andExpect(status().isBadRequest());
  }

  /**
   * Tests a transfer from a non-existent account.
   */
  @Test
  void transferMoneyNonExistentFromAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "NonExistent")
                    .param("accountToId", "Id-2")
                    .param("amount", "100"))
            .andExpect(status().isBadRequest());
  }

  /**
   * Tests a transfer to a non-existent account.
   */
  @Test
  void transferMoneyNonExistentToAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .param("accountFromId", "Id-1")
                    .param("accountToId", "NonExistent")
                    .param("amount", "100"))
            .andExpect(status().isBadRequest());
  }
}
