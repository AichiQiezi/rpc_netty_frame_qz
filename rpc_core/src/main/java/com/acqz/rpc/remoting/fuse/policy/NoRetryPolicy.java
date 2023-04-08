package com.acqz.rpc.remoting.fuse.policy;

import org.apache.curator.RetryPolicy;

/**
 *  No retry
 * @author haofeng
 * @date 2023/4/4 17:05
 */

public class NoRetryPolicy extends AbstractRetryPolicy {

    @Override
    public boolean shouldRetry(int attempt, Throwable lastException) {
        return false;
    }
}
