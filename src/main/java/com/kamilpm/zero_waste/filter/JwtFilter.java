package com.kamilpm.zero_waste.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamilpm.zero_waste.domain.dto.ErrorResponse;
import com.kamilpm.zero_waste.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

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

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
            null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

      }
    } catch (JwtException e) {

      ErrorResponse error = new ErrorResponse(
          e.getMessage(),
          request.getRequestURI(),
          401,
          "Unauthorized");

      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      new ObjectMapper().writeValue(response.getOutputStream(), error);
      return;

    } catch (Exception e) {

      ErrorResponse error = new ErrorResponse(
          e.getMessage(),
          request.getRequestURI(),
          500,
          "Internal server error");
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      new ObjectMapper().writeValue(response.getOutputStream(), error);
      return;
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
