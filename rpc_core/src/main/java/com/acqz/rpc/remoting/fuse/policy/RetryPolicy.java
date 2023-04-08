package com.acqz.rpc.remoting.fuse.policy;

/**
 * @author haofeng
 * @date 2023/4/5 15:20
 */
public interface RetryPolicy {
    /**
     * Method to determine whether to retry a request after a failure
     * @param attempt the number of attempts
     * @param lastException exception generated during the last retry
     * @return true: can retry
     *         false: can not retry
     */
    boolean shouldRetry(int attempt, Throwable lastException);
}
