package com.social.minisocialplatform.sharding;

import java.util.HashMap;
import java.util.Map;

public class ConsistentHashingTest {

    public static void main(String[] args) {

        ConsistentHashing hashing = new ConsistentHashing(100);

        ShardNode shard1 = new ShardNode("shard1");
        ShardNode shard2 = new ShardNode("shard2");
        ShardNode shard3 = new ShardNode("shard3");

        hashing.addNode(shard1);
        hashing.addNode(shard2);
        hashing.addNode(shard3);

        System.out.println("user123 is in " + hashing.getNode("user123").getId());

        System.out.println("user456 is in " + hashing.getNode("user456").getId());

        System.out.println("user789 is in " + hashing.getNode("user789").getId());

        System.out.println("\nBefore adding new shard: shard4");

        Map<String, String> beforeMapping = new HashMap<>();

        for(int i = 1; i <= 100; i++) {
            String userId = "user" + i;
            String shardId = hashing.getNode(userId).getId();

            beforeMapping.put(userId, shardId);

            System.out.println(userId + " is in " + shardId);
        }

        ShardNode shard4 = new ShardNode("shard4");
        hashing.addNode(shard4);

        System.out.println("\nAfter adding new shard: shard4");

        for(int i = 1; i <= 100; i++) {
            String userId = "user" + i;
            System.out.println(userId + " is in " + hashing.getNode(userId).getId());
        }

        int movedUsers = 0;
        for(int i = 1; i <= 100; i++) {
            String userId = "user" + i;
            String oldShard = beforeMapping.get(userId);
            String newShard = hashing.getNode(userId).getId();

            if(!oldShard.equals(newShard)) {
                movedUsers++;
            }
        }
        System.out.println("\nUsers moved after adding shard4: " + movedUsers);
    }
}