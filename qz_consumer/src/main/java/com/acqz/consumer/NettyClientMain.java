package com.acqz.consumer;

import com.acqz.rpc.annotation.RpcScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author haofeng
 * @date 2023/3/10 10:03
 * @description todo
 */
@Slf4j
@RpcScan(basePackage = {"com.acqz"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
