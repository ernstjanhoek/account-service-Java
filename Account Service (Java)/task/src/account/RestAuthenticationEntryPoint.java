package account;

import account.repositories.UserRepository;
import account.usermanager.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.time.LocalDateTime;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final UserRepository userRepository;

    public RestAuthenticationEntryPoint(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{ " +
                "\"timestamp\":\"" + LocalDateTime.now() +"\"," +
                "\"status\":401," +
                "\"error\":\"Unauthorized\"," +
                "\"message\":\"" + authException.getMessage() + "\"," +
                "\"path\":\"" + request.getRequestURI() +"\"" +
                "}");
    }
}