package com.acqz.rpc.remoting.fuse;

import com.acqz.common.enums.State;
import com.acqz.common.utils.TimeUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author haofeng
 * @date 2023/4/4 17:01
 */

public abstract class AbstractCircuitBreaker implements CircuitBreaker{

    private volatile long nextRetryTimestamp = 0;
    protected final int recoveryTimeoutMs;
    protected final AtomicReference<State> currentState = new AtomicReference<>(State.CLOSED);

    public AbstractCircuitBreaker(int recoveryTimeoutMs) {
        this.recoveryTimeoutMs = recoveryTimeoutMs;
    }


    @Override
    public boolean tryPass() {
        if (currentState.get() == State.CLOSED) {
            return true;
        }
        if (currentState.get() == State.OPEN) {
            return retryTimeoutArrived() && fromOpenToHalfOpen();
        }
        return false;
    }

    /**
     * Reset the statistic data.
     */
    abstract void resetStat();


    @Override
    public State currentState() {
        return currentState.get();
    }

    protected boolean fromCloseToOpen() {
        State prev = State.CLOSED;
        if (currentState.compareAndSet(prev, State.OPEN)) {
            updateNextRetryTimestamp();
            return true;
        }
        return false;
    }

    protected boolean retryTimeoutArrived() {
        return TimeUtil.currentTimeMillis() >= nextRetryTimestamp;
    }

    protected void updateNextRetryTimestamp() {
        this.nextRetryTimestamp = TimeUtil.currentTimeMillis() + recoveryTimeoutMs;
    }

    protected boolean fromOpenToHalfOpen() {
        return currentState.compareAndSet(State.OPEN, State.HALF_OPEN);
    }

    protected boolean fromHalfOpenToOpen() {
        State prev = State.HALF_OPEN;
        if (currentState.compareAndSet(prev, State.OPEN)) {
            updateNextRetryTimestamp();
            return true;
        }
        return false;
    }

    protected boolean fromHalfOpenToClose() {
        if (currentState.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
            resetStat();
            return true;
        }
        return false;
    }

    protected void transformToOpen() {
        State cs = currentState.get();
        switch (cs) {
            case CLOSED:
                fromCloseToOpen();
                break;
            case HALF_OPEN:
                fromHalfOpenToOpen();
                break;
            default:
                break;
        }
    }



}


