package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferData;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void processTransaction(TransferData transferData) {
        Account accountFrom = this.accountsRepository.getAccount(transferData.getAccountFromId());
        Account accountTo = this.accountsRepository.getAccount(transferData.getAccountToId());
        validateTransferDetails(accountFrom, transferData.getAmount());
        doTransfer(accountFrom, accountTo, transferData.getAmount());
        notificationService.notifyAboutTransfer(accountFrom, "Transfer is completed!");
    }


    public void doTransfer(Account accountFrom, Account accountTo, BigDecimal amount) {
        // As a solution to prevent possible deadlock accountFrom -> accountTo vs accountTo -> accountFrom
        List<Account> accountList = Arrays.asList(accountFrom, accountTo);
        accountList.sort(Account::compareTo);
        Account firstAccount = accountList.get(0);
        synchronized (firstAccount) {
            Account secondAccount = accountList.get(1);
            synchronized (secondAccount) {
                accountFrom = firstAccount.getAccountId().equals(accountFrom.getAccountId()) ? firstAccount : secondAccount;
                accountTo = firstAccount.getAccountId().equals(accountTo.getAccountId()) ? firstAccount : secondAccount;
                Account.transfer(accountFrom, accountTo, amount);
            }
        }
    }

    private void validateTransferDetails(Account accountFrom, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeBalanceException(
                    "Amount should be positive!");
        }

        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new NegativeBalanceException(
                    "Account id " + accountFrom.getAccountId() + " insufficient balance!");
        }

    }

}
