package xyz.subho.retail.banking.service;

import java.security.Principal;
import java.math.BigDecimal;

import xyz.subho.retail.banking.model.CurrentAccount;
import xyz.subho.retail.banking.model.SavingsAccount;

public interface AccountService {

    CurrentAccount createCurrentAccount();

    SavingsAccount createSavingsAccount();

    void deposit(String accountType, BigDecimal amount, Principal principal);

    void withdraw(String accountType, BigDecimal amount, Principal principal);

}
