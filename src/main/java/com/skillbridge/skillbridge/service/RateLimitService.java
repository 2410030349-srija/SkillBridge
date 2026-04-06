package com.skillbridge.skillbridge.service; // NOSONAR - false positive: package is named

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final ConcurrentMap<String, CounterWindow> counters = new ConcurrentHashMap<>();

    public boolean allowRequest(String key, int limitPerMinute) {
        long nowEpochMs = Instant.now().toEpochMilli();
        long windowStart = nowEpochMs - (nowEpochMs % 60_000);

        CounterWindow window = counters.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStartMs != windowStart) {
                return new CounterWindow(windowStart, 1);
            }

            return new CounterWindow(existing.windowStartMs, existing.count + 1);
        });

        return window.count <= limitPerMinute;
    }

    private static final class CounterWindow {
        private final long windowStartMs;
        private final int count;

        private CounterWindow(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}