package xyz.subho.retail.banking.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class AccountNumberServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void retriesDatabaseCollisions() {
        when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(0, 1);

        int accountNumber = new AccountNumberService(jdbcTemplate).next();

        verify(jdbcTemplate, times(2)).update(anyString(), anyInt());
        assertTrue(accountNumber >= 2323 && accountNumber < 232321474);
    }

    @Test
    void failsAfterRepeatedCollisions() {
        when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> new AccountNumberService(jdbcTemplate).next());
        verify(jdbcTemplate, times(100)).update(anyString(), anyInt());
    }
}
