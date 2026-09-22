package com.urlshortener.service;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository repository;


    public String createShortUrl(UrlRequest request) {

        LocalDateTime now = LocalDateTime.now();

        ShortUrl url = ShortUrl.builder()
                .longUrl(request.longUrl())
                .createdAt(now)
                .expiresAt(now.plusHours(24))
                .clickCount(0L)
                .build();

        url = repository.save(url);

        String shortCode =
                Base62Encoder.encode(url.getId());

        url.setShortCode(shortCode);

        repository.save(url);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {

        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        if (!LocalDateTime.now().isBefore(url.getExpiresAt())) {
            throw new ShortUrlNotFoundException(
                    "Short URL has expired: " + shortCode
            );
        }
        url.setClickCount(url.getClickCount() + 1);

        repository.save(url);

        return url.getLongUrl();
    }

    public UrlStatsResponse getStats(String shortCode) {

        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        return new UrlStatsResponse(
                url.getShortCode(),
                url.getLongUrl(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount()
        );
    }

}