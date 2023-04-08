package com.acqz.common.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author haofeng
 * @date 2023/4/4 18:12
 */

public final class TimeUtil {

    private static volatile long currentTimeMillis;

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Throwable e) {

                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }
}