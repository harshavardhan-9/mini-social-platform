package com.social.minisocialplatform.cache;

import java.util.HashMap;
import java.util.Map;

public class LFUCache<K, V> implements Cache<K, V> {
    private int capacity;
    private Map<K , Node<K, V>> cacheMap;
    private Map<K, Integer> frequencyMap;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.frequencyMap = new HashMap<>();
        
    }

    public V get(K key) {
        Node<K, V> node = cacheMap.get(key);
        if(node!= null) {
            int freq = frequencyMap.get(key);
            frequencyMap.put(key, freq + 1);
            return node.value;
        }
        return null;
    }

    public void evictLFU() {
        K LFUKey = null;
        int minFreq = Integer.MAX_VALUE;
        for(Map.Entry<K, Integer> entry : frequencyMap.entrySet()){
            if(entry.getValue() < minFreq) {
                minFreq = entry.getValue();
                LFUKey = entry.getKey();
            }
        }
        if(LFUKey != null) {
            cacheMap.remove(LFUKey);
            frequencyMap.remove(LFUKey);
        }

    }

    public void put(K key, V value) {
        Node<K, V> node = cacheMap.get(key);
        if(node != null) {
            node.value = value;
            int freq = frequencyMap.get(key);
            frequencyMap.put(key, freq+1);
        }
        else{
            if(cacheMap.size() >= capacity) {
                evictLFU();
            }
            Node<K, V> newNode = new Node<>(key, value);
            cacheMap.put(key, newNode);
            frequencyMap.put(key, 1);
        }
    }

    public void invalidate(K key) {
        Node<K, V> node = cacheMap.get(key);
        if(node != null) {
            cacheMap.remove(key);
            frequencyMap.remove(key);
        }
    }
}