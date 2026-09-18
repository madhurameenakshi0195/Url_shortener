package com.urlshortener.service;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.entity.ShortUrl;
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

        // save first to get ID
        ShortUrl url = ShortUrl.builder()
                .longUrl(request.longUrl())
                .createdAt(LocalDateTime.now())
                .build();

        url = (ShortUrl) repository.save(url);

        // convert ID to Base62
        String shortCode =
                Base62Encoder.encode(url.getId());

        url.setShortCode(shortCode);

        repository.save(url);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {

        try {
            return repository.findByShortCode(shortCode)
                    .orElseThrow(() ->
                            new RuntimeException("URL not found"))
                    .getLongUrl();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}