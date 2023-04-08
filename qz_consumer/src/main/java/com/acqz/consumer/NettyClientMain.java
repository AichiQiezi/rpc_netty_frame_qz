package com.acqz.consumer;

import com.acqz.rpc.annotation.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author haofeng
 * @date 2023/3/10 10:03
 * @description todo
 */
@RpcScan(basePackage = {"com.acqz"})
@SpringBootApplication
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(NettyClientMain.class,args);
    }
}
