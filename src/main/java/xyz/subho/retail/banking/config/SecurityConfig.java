package xyz.subho.retail.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import xyz.subho.retail.banking.service.serviceImpl.UserSecurityServiceImpl;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String SALT = "salt"; // Salt should be protected carefully

    private static final String[] PUBLIC_MATCHERS = { "/webjars/**", "/css/**", "/js/**", "/images/**", "/",
            "/about/**", "/contact/**", "/error/**/*", "/console/**", "/signup", "/admin/**" };

    private final UserSecurityServiceImpl userSecurityService;

    public SecurityConfig(UserSecurityServiceImpl userSecurityService) {
        this.userSecurityService = userSecurityService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .failureUrl("/index?error")
                .defaultSuccessUrl("/userFront")
                .loginPage("/index")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/index?logout")
                .deleteCookies("remember-me")
                .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe.key(SALT).rememberMeParameter("remember-me"))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userSecurityService)
            .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
