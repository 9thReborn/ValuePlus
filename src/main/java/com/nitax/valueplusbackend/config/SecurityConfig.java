package com.nitax.valueplusbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nitax.valueplusbackend.service.impl.UserDetailsService;
import com.nitax.valueplusbackend.utils.enums.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/advertisers/**")
                                        .permitAll()
                                        .requestMatchers("/auth/**").permitAll()
                                        .requestMatchers("/webhooks/subscription/**")
                                        .hasRole(Role.ADVERTISER.name())
                                        .requestMatchers("/webhooks/**").permitAll()
                                        .requestMatchers("/contactRequest/**")
                                        .permitAll()
                                        .requestMatchers("/admin/login")
                                        .permitAll()
                                        .requestMatchers("/pubs/**")
                                        .hasAnyRole(Role.SUPER_ADMIN.name(), Role.ADMIN.name(),Role.PUBLISHER.name())
                                        .requestMatchers("/admin/**")
                                        .hasAnyRole(Role.SUPER_ADMIN.name(), Role.ADMIN.name())
                                        .requestMatchers("/auth/affiliates/**").permitAll()
                                        .requestMatchers("/email/**")
                                        .permitAll()
                                        .requestMatchers("/campaigns/**")
                                        .permitAll()
                                        .requestMatchers("/admin/**")
                                        .permitAll()
                                        .requestMatchers("/cc/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui/**")
                                        .permitAll()
                                        // whitelist actuator endpoint
                                        .requestMatchers("/actuator/**")
                                        .permitAll()
                                        .requestMatchers("/callbacks/antifraud/**")
                                        .permitAll()
                                        .requestMatchers("/api/publishers/**")
                                        .permitAll()
                                        .requestMatchers("/wallet/**").hasAnyRole(Role.SUPER_ADMIN.name(), Role.ADMIN.name(),Role.ADVERTISER.name())
                                        .anyRequest()
                                        .authenticated());

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(
                authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
