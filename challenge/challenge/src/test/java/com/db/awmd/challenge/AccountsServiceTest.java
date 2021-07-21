package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferData;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    private Account accountFrom;
    private Account accountTo;

    private Account accountFrom2;
    private Account accountTo2;
    private BigDecimal balance;

    @Before
    public void initAccounts() {
        balance = new BigDecimal(100);
        accountFrom = new Account("Id-123", balance);
        accountTo = new Account("Id-124", balance);

        accountFrom2 = new Account("Id-125", balance);
        accountTo2 = new Account("Id-126", balance);

        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);
        this.accountsService.createAccount(accountFrom2);
        this.accountsService.createAccount(accountTo2);
    }

    @After
    public void reset() {
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void addAccount() throws Exception {
        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(accountFrom);
    }

    @Test
    public void addAccount_failsOnDuplicateId() throws Exception {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }

    }

    @Test
    public void shouldProcessTransactionTest() throws InterruptedException {
        this.accountsService.processTransaction(new TransferData(accountFrom.getAccountId(), accountTo.getAccountId(), new BigDecimal(10)));
        assertThat(accountFrom.getBalance()).isEqualTo(new BigDecimal(90));
    }

    @Test
    public void shouldThrowAmountExceptionTest () throws InterruptedException {
        try {
            this.accountsService.processTransaction(new TransferData(accountFrom.getAccountId(), accountTo.getAccountId(), new BigDecimal(-1)));
        } catch (NegativeBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Amount should be positive!");
        }
    }

    @Test
    public void shouldThrowNegativeBalanceExceptionTest() throws InterruptedException {
        try {
            this.accountsService.processTransaction(new TransferData(accountFrom.getAccountId(), accountTo.getAccountId(), accountFrom.getBalance().add(balance)));
        } catch (NegativeBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + accountFrom.getAccountId() + " insufficient balance!");
        }
    }

    @Test
    public void shouldDoTransferInThreadSafeNoDeadLocks() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            try {
                firstThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                secondThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread thread3 = new Thread(() -> {
            try {
                thirdThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread thread4 = new Thread(() -> {
            try {
                fourthThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        assertThat(accountFrom.getBalance().add(accountTo.getBalance())).isEqualTo(balance.add(balance));
        assertThat(accountFrom2.getBalance().add(accountTo2.getBalance())).isEqualTo(balance.add(balance));

    }

    private void firstThread() throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            this.accountsService.doTransfer(accountFrom, accountTo, accountFrom.getBalance());
        }
    }

    private void secondThread() throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            this.accountsService.doTransfer(accountTo, accountFrom, accountFrom.getBalance());
        }
    }

    private void thirdThread() throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            this.accountsService.doTransfer(accountFrom2, accountTo2, accountFrom.getBalance());
        }
    }

    private void fourthThread() throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            this.accountsService.doTransfer(accountTo2, accountFrom2, accountFrom.getBalance());
        }
    }
}
