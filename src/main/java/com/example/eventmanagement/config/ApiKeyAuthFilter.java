package com.example.eventmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER= "X-API-KEY";
    private final byte[] clientKeyHash;
    private final byte[] adminKeyHash;

    public ApiKeyAuthFilter(String clientKey, String adminKey) {
        this.clientKeyHash = sha256(clientKey);
        this.adminKeyHash = sha256(adminKey);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        if(key != null){
            byte[] provided =sha256(key);
            if(MessageDigest.isEqual(provided,adminKeyHash)){
        }
    }
}
