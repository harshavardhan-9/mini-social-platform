package com.social.minisocialplatform;

import com.social.minisocialplatform.cache.LRUCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LRUCacheTest {

    @Test
    public void testUpdate() {
        LRUCache<Integer,String> cache = new LRUCache<>(2);
        cache.put(1, "A");
        assertEquals("A", cache.get(1)); // Key 1 should be present
        cache.put(1, "A_updated"); // Update key 1
        assertEquals("A_updated", cache.get(1)); // Key 1 should be updated
    }

    @Test
    public void testEviction() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "A");
        cache.put(2, "B");
        
        assertEquals("A", cache.get(1)); // Access 1 to make it recently used
        cache.put(3, "C"); // Evicts key 2
        
        assertNull(cache.get(2)); // Key 2 should be evicted
        assertEquals("A", cache.get(1)); // Key 1 should still be present
        assertEquals("C", cache.get(3)); // Key 3 should be present

    }

    @Test
    public void testInvalidate() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "A");
        assertEquals("A", cache.get(1)); // Key 1 should be present
        cache.invalidate(1); // Invalidate key 1
        assertNull(cache.get(1)); // Key 1 should be invalidated
    }
}