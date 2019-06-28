package com.uddernetworks.emoji.utils;

public class Thread {
    public static void sleep(long duration) {
        try {
            java.lang.Thread.sleep(duration);
        } catch (InterruptedException e) {}
    }
}
