package com.social.minisocialplatform.cache;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> implements Cache<K, V> {
    private int capacity;
    private Map<K , Node<K, V>> cacheMap;
    private Node<K, V> head;
    private Node<K, V> tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    private void addNode(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node); //remove from current position
        addNode(node); //add to head
    }

    private void evictLRU() {
        Node<K, V> lruNode = tail.prev;
        removeNode(lruNode);
        cacheMap.remove(lruNode.key);
    }

    public V get(K key) {
        Node<K, V> node = cacheMap.get(key);
        if(node!= null) {   
            moveToHead(node);
            return node.value;
        }
        return null;
    }
    
    public void put(K key, V value) {
        Node<K, V> node = cacheMap.get(key);
        if(node != null) {
            node.value = value;
            moveToHead(node);
        }
        else{
            if(cacheMap.size() >= capacity) {
                evictLRU();
            }
            Node<K, V> newNode = new Node<>(key, value);
            cacheMap.put(key, newNode);
            addNode(newNode);

        }
    }

    public void invalidate(K key) {
        Node<K, V> node = cacheMap.get(key);
        if(node != null) {
            removeNode(node);
            cacheMap.remove(key);
        }
    }
}