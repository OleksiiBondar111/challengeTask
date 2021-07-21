package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Slf4j
public class Account implements Comparable {

    @NotNull
    @NotEmpty
    private final String accountId;

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal balance;

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
    }

    @JsonCreator
    public Account(@JsonProperty("accountId") String accountId,
                   @JsonProperty("balance") BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }


    public static void transfer(Account accountFrom, Account accountTo, BigDecimal amount) {
        accountFrom.withdraw(amount);
        accountTo.deposit(amount);
    }

    @Override
    public int compareTo(Object o) {
        return this.accountId.compareTo(((Account) o).getAccountId());
    }
}
