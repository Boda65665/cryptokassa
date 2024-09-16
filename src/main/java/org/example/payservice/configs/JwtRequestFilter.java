package org.example.payservice.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.payservice.Repositories.UserRepository;
import org.example.payservice.Service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtRequestFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String token = jwtService.resolveToken(request);
        if (jwtService.isTokenValid(token)) {
            SecurityContextHolder.getContext().setAuthentication(jwtService.getAuthentication(token));
        } else {
        SecurityContextHolder.clearContext();
    }
    try {
        filterChain.doFilter(request, response);
    } catch (ServletException | IOException e) {
        logger.error(e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}
}