package com.social.minisocialplatform.sharding;

public class ShardRouter {
    private ConsistentHashing hashing;

    public ShardRouter() {
        hashing = new ConsistentHashing(100);

        hashing.addNode(new ShardNode("shard1"));
        hashing.addNode(new ShardNode("shard2"));
        hashing.addNode(new ShardNode("shard3"));
    }

    public String getShard(String userId) {

        return hashing.getNode(userId).getId();
    }
}
