package com.ultrahpm.productservice.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ultrahpm.productservice.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class TwoTierCacheService {

    private static final Logger log = LoggerFactory.getLogger(TwoTierCacheService.class);
    private static final String REDIS_PREFIX = "product:";

    // L1 Cache: Ultra-fast local JVM cache (Caffeine)
    private final Cache<String, Product> l1Cache;

    // L2 Cache: Distributed cache (Redis)
    private final RedisTemplate<String, Object> redisTemplate;

    public TwoTierCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // Configure L1 cache: store up to 10,000 items, expire after 5 minutes
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    /**
     * Get a product using the L1 -> L2 -> L3 (Database) strategy.
     */
    public Product getProduct(String productId, Supplier<Optional<Product>> databaseFallback) {
        // 1. Check L1 Cache (Caffeine)
        Product product = l1Cache.getIfPresent(productId);
        if (product != null) {
            log.debug("L1 Cache HIT (Caffeine) for productId={}", productId);
            return product;
        }

        // 2. Check L2 Cache (Redis)
        String redisKey = REDIS_PREFIX + productId;
        product = (Product) redisTemplate.opsForValue().get(redisKey);
        if (product != null) {
            log.debug("L2 Cache HIT (Redis) for productId={}. Backfilling L1.", productId);
            l1Cache.put(productId, product);
            return product;
        }

        // 3. Cache Miss (L1 & L2). Fallback to Database (L3)
        log.debug("Cache MISS for productId={}. Querying database (L3).", productId);
        product = databaseFallback.get().orElseThrow(() -> 
            new com.ultrahpm.productservice.exception.ProductNotFoundException("Product not found: " + productId)
        );

        // Backfill both caches
        log.debug("Backfilling L1 and L2 caches for productId={}", productId);
        l1Cache.put(productId, product);
        redisTemplate.opsForValue().set(redisKey, product, Duration.ofMinutes(30));

        return product;
    }

    /**
     * Invalidate caches upon update or deletion.
     */
    public void evict(String productId) {
        log.debug("Evicting productId={} from L1 and L2 caches", productId);
        l1Cache.invalidate(productId);
        redisTemplate.delete(REDIS_PREFIX + productId);
    }
}
