package com.acqz.provider;

import com.acqz.common.service.Hello;
import com.acqz.common.service.HelloService;
import com.acqz.rpc.annotation.RpcService;

/**
 * @author haofeng
 * @date 2023/3/10 9:59
 * @description todo
 */
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(Hello hello) {
        System.out.println("HelloServiceImpl收到: {}."+ hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        System.out.println("HelloServiceImpl返回: {}."+ result);
        return result;
    }
}
