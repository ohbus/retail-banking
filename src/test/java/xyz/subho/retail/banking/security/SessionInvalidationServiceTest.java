package xyz.subho.retail.banking.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class SessionInvalidationServiceTest {

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private UserDetails user;

    @Mock
    private SessionInformation sessionInformation;

    @Test
    void expiresActiveSessionsForMatchingUserOnly() {
        when(user.getUsername()).thenReturn("alice");
        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(user, "not-a-user"));
        when(sessionRegistry.getAllSessions(user, false)).thenReturn(List.of(sessionInformation));

        new SessionInvalidationService(sessionRegistry).expireSessions("alice");

        verify(sessionInformation).expireNow();
    }
}
