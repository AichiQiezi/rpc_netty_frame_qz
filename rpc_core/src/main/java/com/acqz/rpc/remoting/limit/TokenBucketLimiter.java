package com.acqz.rpc.remoting.limit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * easy to understand from the name
 *
 * @author haofeng
 * @date 2023/4/5 22:10
 */
@Slf4j
public class TokenBucketLimiter {

    private static final int CAPACITY_NUM = 2;

    public long lastTime = System.nanoTime();
    public final int capacity;
    /**
     * token generation rate per second
     */
    public int rate = 2;
    /**
     * current the number of token
     */
    public final AtomicInteger tokens;

    public TokenBucketLimiter(){
        this(CAPACITY_NUM);
    }

    public TokenBucketLimiter(int capacity){
        this.capacity = capacity;
        tokens = new AtomicInteger(capacity);
    }

    /**
     * @param applyCount the token to be consumed
     * @return {@code true} restricted
     * {@code false} not restricted
     */
    public synchronized boolean isLimited(int applyCount) {
        long now = System.nanoTime();
        long gap = now - lastTime;

        // calculates the number of tokens generated in the time period
        int reverse_permits = (int) (gap * rate / 1000000000L);
        int all_permits = tokens.get() + reverse_permits;

        tokens.set(Math.min(capacity, all_permits));
        log.info("tokens {} capacity {} gap {} reverse_permits {}", tokens, capacity, gap, reverse_permits);

        if (tokens.get() >= applyCount) {
            tokens.getAndAdd(-applyCount);
            lastTime = now;
            return false;
        }
        return true;
    }

    /**
     * @see
     * @return
     */
    public boolean tryAcquire() {
        return !isLimited(1);
    }
}
