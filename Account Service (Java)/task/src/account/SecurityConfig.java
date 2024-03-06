package account;

import account.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint(UserRepository userRepository) {
        return new RestAuthenticationEntryPoint(userRepository);
    }
    @Bean
    public AccountAccessDeniedHandler accountAccessDeniedHandler() {
        return new AccountAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .httpBasic().authenticationEntryPoint(restAuthenticationEntryPoint(userRepository))// Handle auth errors
                .and()
                .exceptionHandling().accessDeniedHandler(accountAccessDeniedHandler())  // 403
                .and()
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                .headers().frameOptions().disable() // For the H2 console
                .and()
                .authorizeHttpRequests(auth -> auth  // manage access
                        .requestMatchers("/actuator/shutdown").permitAll()      // needs to run test
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole(
                                "USER",
                                "ACCOUNTANT"
                        )
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyRole(
                                "USER",
                                "ACCOUNTANT",
                                "ADMINISTRATOR"
                        )
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/{email}").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/security/events/").hasRole("AUDITOR")
                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );
        return http.build();
    }
}