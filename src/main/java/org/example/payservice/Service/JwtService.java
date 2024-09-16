package org.example.payservice.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.payservice.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Duration validityInMilliseconds;
    private final UserRepository userRepository;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        if (claims==null)return null;
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                    .getBody();
        }
        catch (Exception | Error err){
            return null;
        }
    }

    public String generateToken(String data) {
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + validityInMilliseconds.toMillis());
        return Jwts.builder().setSubject(data)
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            String email = extractEmail(token);
            return (email!=null && userRepository.findByEmail(email)!= null) && !isTokenExpired(token);
        }
        catch (Exception | Error e){
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Auth");
    }

    public Authentication getAuthentication(String token) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        String id = String.valueOf(extractEmail(token));
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(id),
                null,
                authorities
        );
    }
}