package com.example.bank.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                logger.debug("Attempting to validate JWT token: {}", token);
                String username = jwtUtil.validateToken(token);
                logger.debug("Token validated successfully for user: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Получаем UserDetails с ролями
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("User details loaded for: {}, with authorities: {}", username, userDetails.getAuthorities());

                    // Создаём authToken с authorities
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext for user: {}", username);
                } else {
                    logger.warn("Either username is null or authentication already exists in context");
                }
            } catch (Exception e) {
                // Логируем ошибку валидации токена, но продолжаем выполнение
                // Токен невалидный, пользователь останется неаутентифицированным
                logger.error("JWT token validation failed: " + e.getMessage(), e);
            }
        } else {
            logger.debug("No valid Authorization header found");
        }
        filterChain.doFilter(request, response);
    }
}