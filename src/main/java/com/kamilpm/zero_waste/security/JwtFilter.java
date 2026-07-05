package com.kamilpm.zero_waste.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kamilpm.zero_waste.service.JwtService;
import com.kamilpm.zero_waste.service.impl.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final MyUserDetailsService myUserDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String token = getJWTfromRequest(request);

      if (token != null && jwtService.isTokenValid(token)) {

        String email = jwtService.getEmailFromToken(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

          UserDetails userDetails = myUserDetailsService.loadUserByUsername(email);

          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
              null, userDetails.getAuthorities());

          authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

      }

    } catch (Exception e) {

      SecurityContextHolder.clearContext();

    }

    filterChain.doFilter(request, response);
  }

  private String getJWTfromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    } else {
      return null;
    }
  }

}
