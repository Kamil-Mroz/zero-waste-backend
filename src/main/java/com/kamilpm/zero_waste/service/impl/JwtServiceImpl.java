package com.kamilpm.zero_waste.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.MyUserDetails;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.exception.TokenException;
import com.kamilpm.zero_waste.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {

    User user = ((MyUserDetails) userDetails).getUser();
    return Jwts.builder()
        .claims()
        .add("id", user.getId())
        .add("roles", user.getRoles())
        .add(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .and().signWith(getKey()).compact();
  }

  public String getEmailFromToken(String token) {
    try {
      return extractClaim(token, Claims::getSubject);
    } catch (ExpiredJwtException e) {
      throw new TokenException("Token expired");
    } catch (JwtException e) {
      throw new TokenException("Invalid token");
    }
  }

  public Date extractExpiration(String token) {
    try {
      return extractClaim(token, Claims::getExpiration);
    } catch (ExpiredJwtException e) {
      throw new TokenException("Token expired");
    } catch (JwtException e) {
      throw new TokenException("Invalid token");
    }
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    final Claims claims = extractAllClaims(token);
    return resolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtException("Token invalid or expired");
    }

  }

  private SecretKey getKey() {
    byte[] keyBytes = secretKey.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
