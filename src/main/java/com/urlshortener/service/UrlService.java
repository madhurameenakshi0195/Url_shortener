package com.urlshortener.service;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    // =========================
    // CREATE SHORT URL
    // =========================
    public UrlResponse createShortUrl(UrlRequest request) {

        ShortUrl url = new ShortUrl();

        url.setLongUrl(request.longUrl());
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);

        // Save first so PostgreSQL generates the ID
        url = urlRepository.save(url);

        // Generate Base62 short code
        String shortCode = Base62Encoder.encode(url.getId());

        url.setShortCode(shortCode);

        // Save the short code
        urlRepository.save(url);

        return new UrlResponse(
                "http://localhost:8080/" + shortCode
        );
    }

    // =========================
    // GET LONG URL
    // =========================
    // Redis caches the result of this method.
    // IMPORTANT: No click-count logic here.
    @Cacheable(value = "urls", key = "#shortCode")
    public String getLongUrl(String shortCode) {

        System.out.println("🔥 REDIS CACHE MISS → Going to PostgreSQL");

        ShortUrl url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        return url.getLongUrl();
    }

    // =========================
    // INCREMENT CLICK COUNT
    // =========================
    public void incrementClickCount(String shortCode) {

        ShortUrl url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        Long currentCount = url.getClickCount();

        if (currentCount == null) {
            currentCount = 0L;
        }

        url.setClickCount(currentCount + 1);

        urlRepository.save(url);

        System.out.println(
                "📈 Click count increased for "
                        + shortCode
                        + " → "
                        + url.getClickCount()
        );
    }

    // =========================
    // GET URL STATS
    // =========================
    public UrlStatsResponse getStats(String shortCode) {

        ShortUrl url = urlRepository.findByShortCode(shortCode)
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

    // =========================
    // DELETE / EVICT CACHE
    // =========================
    @CacheEvict(value = "urls", key = "#shortCode")
    public void evictCache(String shortCode) {

        System.out.println(
                "🗑️ Redis cache evicted for: " + shortCode
        );
    }
}