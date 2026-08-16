package com.popobob.giftcard.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveMobileBucket(String mobile) {
        return cache.computeIfAbsent("mobile_" + mobile, k -> {
            Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    public Bucket resolveIpBucket(String ip) {
        return cache.computeIfAbsent("ip_" + ip, k -> {
            Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofHours(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
}
