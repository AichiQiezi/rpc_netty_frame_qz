package com.acqz.consumer;

import com.acqz.common.service.Hello;
import com.acqz.common.service.HelloService;
import com.acqz.rpc.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author haofeng
 * @date 2023/3/10 10:04
 * @description todo
 */
@Component
@Slf4j
public class HelloController {
    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        while (true) {
            Thread.sleep(2000);
            System.out.println(helloService.sayHello(new Hello("111", "222")));
        }
    }
}
