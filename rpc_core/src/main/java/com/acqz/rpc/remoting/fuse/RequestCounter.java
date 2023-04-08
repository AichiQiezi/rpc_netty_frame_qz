package com.acqz.rpc.remoting.fuse;

import java.util.concurrent.atomic.LongAdder;

/**
 * This class is used to count the number of successful and failed requests
 * @author haofeng
 * @date 2023/4/4 17:38
 */

public class RequestCounter {
//    private final AtomicInteger successCount = new AtomicInteger(0);
//    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final LongAdder totalCount;
    private final LongAdder failureCount;
    public RequestCounter(){
        totalCount = new LongAdder();
        failureCount = new LongAdder();
    }
    public void incrementTotal() {
        totalCount.increment();
    }

    public void incrementFailed() {
        failureCount.increment();
    }

    public double getFailedCount() {
        return failureCount.intValue();
    }
    public double getTotalCount(){return totalCount.intValue();}

    public void reset() {
        totalCount.reset();
        failureCount.reset();
    }
}
