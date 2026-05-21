package com.social.minisocialplatform.sharding;

import java.util.TreeMap;

public class ConsistentHashing {
    private TreeMap<Integer, ShardNode> ring;

    private int virtualNodes;

    public ConsistentHashing(int virtualNodes) {
        this.ring = new TreeMap<>();
        this.virtualNodes = virtualNodes;
    }

    private int hash(String key) {
        return Math.abs(key.hashCode());
    }

    public void addNode(ShardNode node) {
        for(int i = 0; i< virtualNodes; i++) {
            String virtualNodeKey = node.getId() + "-" + i;
            
            int hash = hash(virtualNodeKey);
            ring.put(hash, node);
        }
    }

    public void getNode(String key) {
        if(ring.isEmpty()) {
            return;
        }
        int hash = hash(key);

        Integer nodeHash = ring.ceilingKey(hash);
        if(nodeHash == null) {
            nodeHash = ring.firstKey();
        }
        return ring.get(nodeHash);
    }

    public void removeNode(ShardNode node) {
    for(int i = 0; i < virtualNodes; i++) {
        String virtualNodeKey = node.getId() + "-" + i;

        int hash = hash(virtualNodeKey);
        ring.remove(hash);
    }
}
}