package com.example.demo.domain;

import jakarta.inject.Singleton;
import jakarta.inject.Inject; // Import for Dagger (optional, but good practice for domain objects that are singletons)

@Singleton // Mark as a Dagger singleton
public class LikeCounter {

    @Inject
    public LikeCounter() {
        // Dagger will use this constructor to create an instance
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
}
