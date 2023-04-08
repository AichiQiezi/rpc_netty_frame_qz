package com.acqz.rpc.remoting.fuse;

import com.acqz.common.enums.State;

/**
 * @author haofeng
 * @date 2023/4/4 18:45
 */
public interface CircuitBreaker {
    /**
     * @return {@code true} if permission acquired and {@code false} otherwise
     */
    boolean tryPass();

    /**
     * Get current state of the circuit breaker
     * @return
     */
    State currentState();

    /**
     *  Callback method: triggered when request is approved and completed
     */
    void onRequestComplete(Throwable error);
}
