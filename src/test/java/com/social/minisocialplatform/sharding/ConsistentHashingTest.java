package com.social.minisocialplatform.sharding;

public class ConsistentHashingTest {

    public static void main(String[] args) {

        ConsistentHashing hashing = new ConsistentHashing(3);

        ShardNode shard1 = new ShardNode("shard1");
        ShardNode shard2 = new ShardNode("shard2");
        ShardNode shard3 = new ShardNode("shard3");

        hashing.addNode(shard1);
        hashing.addNode(shard2);
        hashing.addNode(shard3);

        System.out.println("user123 is in " + hashing.getNode("user123").getId());
        System.out.println("user456 is in " + hashing.getNode("user456").getId());
        System.out.println("user789 is in " + hashing.getNode("user789").getId());

        System.out.println("\n Before adding new shard: shard4");
        for(int i=1; i <= 10; i++) {
            String userId = "user" + i;
            System.out.println(userId + " is in " + hashing.getNode(userId).getId());
        }

        ShardNode shard4 = new ShardNode("shard4");
        hashing.addNode(shard4);

        System.out.println("\n After adding new shard: shard4");
        for(int i=1; i <= 10; i++) {
            String userId = "user" + i;
            System.out.println(userId + " is in " + hashing.getNode(userId).getId());
        }
    }
}