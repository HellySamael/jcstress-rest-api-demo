package com.example.demo.domain;

public class LikeCounter {

    public LikeCounter() {
    }

    private int count = 0;

    public void increment() {
        // Introduce a small artificial delay to increase the chance of race condition
        //try {
        //    Thread.sleep(1); // 1 millisecond delay
        //} catch (InterruptedException e) {
        //    Thread.currentThread().interrupt();
        //}
        count++;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        count = 0;
    }
}
