package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public record UrlRequest(
        @NotBlank
        String longUrl
) {
}