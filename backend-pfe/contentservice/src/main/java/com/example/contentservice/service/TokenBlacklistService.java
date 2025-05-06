package com.example.contentservice.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {

    private final Set<String> blackList = new HashSet<>();

    public boolean isTokenRevoked(String token) {
        return blackList.contains(token);
    }

    public void revokeToken(String token) {
        blackList.add(token);
    }
}