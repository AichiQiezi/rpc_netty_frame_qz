package com.acqz.rpc.remoting.fuse;

import com.acqz.common.enums.State;

/**
 * fuse according to the number of anomalies
 * @author haofeng
 * @date 2023/4/4 18:44
 */

public class ExceptionCircuitBreaker extends AbstractCircuitBreaker{

    private static final int MIN_REQUEST_AMOUNT = 3;
    private static final double THRESHOLD_RADIO = 0.3;

    /**
     * Minimum number of requests that can trigger circuit breaking
     */
    private final int minRequestAmount;
    /**
     * Abnormal proportion threshold
     */
    private final double threshold;
    private final RequestCounter requestCounter;

    public ExceptionCircuitBreaker(){
        this(MIN_REQUEST_AMOUNT,THRESHOLD_RADIO);
    }

    public ExceptionCircuitBreaker(int minRequestAmount, double threshold) {
        super(2000);
        this.minRequestAmount = minRequestAmount;
        this.threshold = threshold;
        requestCounter = new RequestCounter();
    }


    @Override
    public void onRequestComplete(Throwable error) {
        if (error != null){
            requestCounter.incrementFailed();
        }
        requestCounter.incrementTotal();
        handleStateChangeWhenThresholdExceeded(error);
    }

    private void handleStateChangeWhenThresholdExceeded(Throwable error) {
        if (currentState.get() == State.OPEN) {
            return;
        }

        if (currentState.get() == State.HALF_OPEN) {
            if (error == null) {
                fromHalfOpenToClose();
            } else {
                fromHalfOpenToOpen();
            }
            return;
        }

        // state == closed
        double totalCount = requestCounter.getTotalCount();
        double failedCount = requestCounter.getFailedCount();
        if (totalCount < minRequestAmount){
            return;
        }
        double errCount = failedCount;
        errCount = errCount / totalCount;
        if (errCount > threshold){
            transformToOpen();
        }
    }

    @Override
    protected void resetStat() {
        // Reset counter
        requestCounter.reset();
    }

    public RequestCounter getRequestCounter(){
        return requestCounter;
    }

}
