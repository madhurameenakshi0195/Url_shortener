package com.urlshortener.controller;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService service;

    @PostMapping("/api/urls")
    public UrlResponse create(
            @Valid @RequestBody UrlRequest request
    ) {

        String code = service.createShortUrl(request);

        return new UrlResponse(
                "http://localhost:8080/" + code
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable String code
    ) {

        String longUrl = service.getLongUrl(code);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }

    @GetMapping("/api/urls/{code}/stats")
    public UrlStatsResponse stats(
            @PathVariable String code
    ) {
        return service.getStats(code);
    }

}