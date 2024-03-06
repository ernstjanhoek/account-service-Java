package account;


import account.Entities.AccountGroups;
import account.Entities.SecurityEvent;
import account.repositories.SecurityEventRepository;
import account.repositories.UserRepository;
import account.usermanager.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.util.Optional;

@Component
public class AccountEventListener {
    private final UserRepository userRepository;
    private final SecurityEventRepository securityEventRepository;

    @Autowired
    private HttpServletRequest request;
    AccountEventListener(
            UserRepository userRepository,
            SecurityEventRepository securityEventRepository) {
        this.userRepository = userRepository;
        this.securityEventRepository = securityEventRepository;
    }
    @EventListener
    void handleFailedLogin(AuthenticationFailureBadCredentialsEvent event) {
        securityEventRepository.save(
                new SecurityEvent(
                        LocalDate.now(),
                        "LOGIN_FAILED",
                        event.getAuthentication().getName(),
                        request.getRequestURI(),
                        request.getRequestURI()
                )
        );
        Optional<User> dbResult = userRepository.findUserByEmail(event.getAuthentication().getName());
        dbResult.ifPresent(a -> {
            a.incrementFailedLogin();
            if (a.getFailedLoginAttempts() >= 5 && !a.getRoles().contains(new AccountGroups("ROLE_ADMINISTRATOR"))) {
                a.setLocked(true);
                securityEventRepository.save(
                        new SecurityEvent(
                                LocalDate.now(),
                                "BRUTE_FORCE",
                                a.getEmail(),
                                request.getRequestURI(),
                                request.getRequestURI()
                        )
                );
                securityEventRepository.save(
                        new SecurityEvent(
                                LocalDate.now(),
                                "LOCK_USER",
                                a.getEmail(),
                                "Lock user " + a.getEmail(),
                                request.getRequestURI()
                        )
                );
            }
            userRepository.save(a);
        });
    }
    @EventListener
    void handleAuthenticationSucess(AuthenticationSuccessEvent event) {
        Optional<User> dbResult = userRepository.findUserByEmail(event.getAuthentication().getName());
        dbResult.ifPresent(a -> {
            a.setFailedLoginAttempts(0);
            userRepository.save(a);
        });
    }
    @EventListener
    void handleUnauthorized(AuthorizationDeniedEvent event) {
        Optional<User> dbResult = userRepository.findUserByEmail(event.getAuthentication().get().getName());
        dbResult.ifPresent(a -> {
            securityEventRepository.save(
                    new SecurityEvent(
                            LocalDate.now(),
                            "ACCESS_DENIED",
                            a.getEmail(),
                            request.getRequestURI(),
                            request.getRequestURI()
                    )
            );
        });
    }
}