package xyz.subho.retail.banking.security;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SessionInvalidationService {

    private final SessionRegistry sessionRegistry;

    public SessionInvalidationService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void expireSessions(String username) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .filter(user -> username.equals(user.getUsername()))
                .flatMap(user -> sessionRegistry.getAllSessions(user, false).stream())
                .forEach(SessionInformation::expireNow);
    }
}
