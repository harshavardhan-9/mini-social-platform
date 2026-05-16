package com.social.minisocialplatform.benchmark;

import com.social.minisocialplatform.cache.LFUCache;
import com.social.minisocialplatform.cache.LRUCache;
import java.util.Random;

public class CacheBenchmark {
    public static void main(String[] args) {
        LRUCache<Integer, String> lruCache = new LRUCache<>(50);
        LFUCache<Integer, String> lfuCache = new LFUCache<>(50);

        for(int i = 0; i < 50; i++) {
            lruCache.put(i, "Value" + i);
            lfuCache.put(i, "Value" + i);
        }

        int lruHits = 0;
        int lfuHits = 0;
        int totalRequests = 1000;
        Random rand = new Random();

        for(int i=0; i<totalRequests; i++) {
            int probability = rand.nextInt(100);
            int key;

            //80% requests to popular keys
            if(probability <80) {
                key = rand.nextInt(20);
            }
            //20% 
            else {
                key = 20 + rand.nextInt(80);
            }   

            if(lruCache.get(key) != null) {
                lruHits++;
            }

            if(lfuCache.get(key) != null) {
                lfuHits++;
            }
        }

        System.out.println("LRU Hits Ratio: " + (double) lruHits / totalRequests);
        System.out.println("LFU Hits Ratio: " + (double) lfuHits / totalRequests);
    }
}
