package com.social.minisocialplatform.replication;

public class LeaderFollowerTest {

    public static void main(String[] args) throws InterruptedException {
        LeaderFollowerReplication replication = new LeaderFollowerReplication();

        replication.write("user123", "Hello World!");

        System.out.println("\nReading immediately after write:");
        System.out.println("Leader: " + replication.readFromLeader("user123"));

        Thread.sleep(500); // Short sleep to show follower is not updated yet
        System.out.println("Follower: " + replication.readFromFollower("user123"));

        Thread.sleep(1500); // Wait for replication to complete
        System.out.println("\nReading after replication delay:");
        System.out.println("Leader: " + replication.readFromLeader("user123"));
        System.out.println("Follower: " + replication.readFromFollower("user123"));
    }
}
