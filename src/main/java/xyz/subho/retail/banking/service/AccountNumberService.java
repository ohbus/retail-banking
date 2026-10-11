package xyz.subho.retail.banking.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberService {

    private static final int MIN_ACCOUNT_NUMBER = 2323;
    private static final int MAX_ACCOUNT_NUMBER = 232321474;
    private static final int MAX_ATTEMPTS = 100;
    private static final String RESERVE_ACCOUNT_NUMBER =
            "INSERT IGNORE INTO account_number_registry (account_number) VALUES (?)";

    private final JdbcTemplate jdbcTemplate;

    public AccountNumberService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int next() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int accountNumber = ThreadLocalRandom.current().nextInt(MIN_ACCOUNT_NUMBER, MAX_ACCOUNT_NUMBER);
            if (jdbcTemplate.update(RESERVE_ACCOUNT_NUMBER, accountNumber) == 1) {
                return accountNumber;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique account number");
    }
}
