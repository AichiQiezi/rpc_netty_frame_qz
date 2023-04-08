package com.acqz.rpc.remoting.fuse.policy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  指数回退重试策略,
 *  client periodically retries failed requests and increase the latency between requests
 * @author haofeng
 * @date 2023/4/4 17:09
 */

public class ExponentialBackoffRetryPolicy extends AbstractRetryPolicy {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_INITIAL_BACKOFF = 150L;
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;

    private final int maxAttempts;
    private final long initialBackoff;
    private final double backoffMultiplier;

    public ExponentialBackoffRetryPolicy() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_BACKOFF, DEFAULT_BACKOFF_MULTIPLIER);
    }

    public ExponentialBackoffRetryPolicy(int maxAttempts, long initialBackoff, double backoffMultiplier) {
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.backoffMultiplier = backoffMultiplier;
    }

    @Override
    public boolean shouldRetry(int attempt, Throwable lastException) {
        if (attempt > maxAttempts) {
            return false;
        } else {
            long backoff = initialBackoff * (long) Math.pow(backoffMultiplier, attempt - 1);
            try {
                TimeUnit.MILLISECONDS.sleep(backoff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }
    }
}
