package com.example.mediaservice.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    public String getEmailFromToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && authentication instanceof JwtAuthenticationToken jwtAuth) {

                Jwt jwt = (Jwt) jwtAuth.getPrincipal();

                String email = jwt.getClaimAsString("email");
                if (email != null) return email;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email from token", e);
        }

        throw new RuntimeException("Email not found in token");
    }
}
