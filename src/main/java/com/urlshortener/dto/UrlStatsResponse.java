package com.urlshortener.dto;

import java.time.LocalDateTime;

public record UrlStatsResponse(
        String shortCode,
        String longUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long clickCount
) {
}