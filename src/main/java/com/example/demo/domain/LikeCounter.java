package com.example.demo.domain;

import jakarta.inject.Singleton;
import jakarta.inject.Inject; // Import for Dagger (optional, but good practice for domain objects that are singletons)

import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger

@Singleton // Mark as a Dagger singleton
public class LikeCounter {

    @Inject
    public LikeCounter() {
        // Dagger will use this constructor to create an instance
    }

    private final AtomicInteger count = new AtomicInteger(0); // Use AtomicInteger for thread-safety

    public void increment() {
        // Introduce a small artificial delay to increase the chance of race condition
        //try {
        //    Thread.sleep(1); // 1 millisecond delay
        //} catch (InterruptedException e) {
        //    Thread.currentThread().interrupt();
        //}
        count.incrementAndGet(); // Atomically increment the count
    }

    public int getCount() {
        return count.get(); // Get the current value
    }

    public void reset() {
        count.set(0); // Atomically set the count to 0
    }
}
