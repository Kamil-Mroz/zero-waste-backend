package com.kamilpm.zero_waste.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kamilpm.zero_waste.security.JwtAuthEntryPoint;
import com.kamilpm.zero_waste.security.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtFilter jwtFilter;
  private final UserDetailsService userDetailsService;
  private final JwtAuthEntryPoint authEntryPoint;
  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))
        .authorizeHttpRequests(
            (authorize) -> authorize
                .requestMatchers("/api/v{version}/auth/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v{version}/blogs/own").hasAnyRole("ADMIN", "WRITER")
                .requestMatchers(HttpMethod.GET, "/api/v{version}/blogs/**").permitAll()
                .requestMatchers("/api/v{version}/blogs/**").hasAnyRole("ADMIN", "WRITER")
                .requestMatchers("/api/docs/**").permitAll()
                .requestMatchers("/ws/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v{version}/categories/**").permitAll()
                .requestMatchers("/api/v{version}/categories/**").hasRole("ADMIN")

                .requestMatchers("/api/v{version}/items/own").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v{version}/items/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v{version}/images/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v{version}/profiles/**").permitAll()

                .requestMatchers("/api/v{version}/users/**").hasRole("ADMIN")
                .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean

  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(Arrays.asList("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      PasswordEncoder passwordEncoder)
      throws Exception {

    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);

    return new ProviderManager(provider);

  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
