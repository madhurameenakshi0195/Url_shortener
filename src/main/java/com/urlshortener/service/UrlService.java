package com.urlshortener.service;

import com.urlshortener.dto.CachedUrl;
import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository repository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;


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

        // 1. Check Redis first
        String cachedValue = redisService.get(shortCode);

        if (cachedValue != null) {

            try {
                CachedUrl cachedUrl =
                        objectMapper.readValue(cachedValue, CachedUrl.class);

                // 2. Check whether cached URL has expired
                if (!LocalDateTime.now().isBefore(cachedUrl.expiresAt())) {
                    redisService.delete(shortCode);

                    throw new ShortUrlNotFoundException(
                            "Short URL has expired: " + shortCode
                    );
                }

                // 3. Cache HIT
                ShortUrl url = repository.findByShortCode(shortCode)
                        .orElseThrow(() ->
                                new ShortUrlNotFoundException(
                                        "Short URL not found: " + shortCode
                                )
                        );

                url.setClickCount(url.getClickCount() + 1);
                repository.save(url);

                return cachedUrl.longUrl();

            } catch (JsonProcessingException e) {
                redisService.delete(shortCode);
            }
        }

        // 4. Cache MISS → go to PostgreSQL
        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        // 5. Check database expiration
        if (!LocalDateTime.now().isBefore(url.getExpiresAt())) {
            throw new ShortUrlNotFoundException(
                    "Short URL has expired: " + shortCode
            );
        }

        // 6. Put URL into Redis
        try {
            CachedUrl cachedUrl =
                    new CachedUrl(
                            url.getLongUrl(),
                            url.getExpiresAt()
                    );

            String json = objectMapper.writeValueAsString(cachedUrl);

            Duration ttl = Duration.between(
                    LocalDateTime.now(),
                    url.getExpiresAt()
            );

            redisService.save(shortCode, json, ttl);

        } catch (JsonProcessingException e) {
            // Redis caching failure should not break URL redirection
        }

        // 7. Increment click count in PostgreSQL
        url.setClickCount(url.getClickCount() + 1);
        repository.save(url);

        // 8. Redirect using the URL
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