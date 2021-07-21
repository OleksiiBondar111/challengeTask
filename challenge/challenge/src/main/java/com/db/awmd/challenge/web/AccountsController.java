package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferData;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.AmountValueException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transfer(@RequestBody TransferData transferData) {
        log.info("Transfer {}", transferData);

        try {
            this.accountsService.processTransaction(transferData);
        } catch (AccountNotFoundException anfe) {
            return new ResponseEntity<>(anfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NegativeBalanceException | AmountValueException nbe) {
            return new ResponseEntity<>(nbe.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

}
