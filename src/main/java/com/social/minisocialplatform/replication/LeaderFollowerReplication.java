package com.social.minisocialplatform.replication;

import java.util.HashMap;
import java.util.Map;

public class LeaderFollowerReplication {
    private Map<String, String> leaderDB = new HashMap<>();
    private Map<String, String> followerDB = new HashMap<>();

    public void write(String key, String value) {
        System.out.println("Writing to leader: " + key + " = " + value);
        leaderDB.put(key, value);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            followerDB.put(key, value);
            System.out.println("Replicated to follower: " + key + " = " + value);
        }).start();
    }

    public String readFromLeader(String key) {
        System.out.println("Reading from leader: " + key);
        return leaderDB.get(key);
    }

    public String readFromFollower(String key) {
        System.out.println("Reading from follower: " + key);
        return followerDB.get(key);
    }
}
