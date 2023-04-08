package com.acqz.consumer;

import com.acqz.common.service.Hello;
import com.acqz.common.service.HelloService;
import com.acqz.rpc.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author haofeng
 * @date 2023/3/10 10:04
 * @description todo
 */
@RestController
public class HelloController {
    @RpcReference(version = "version1", group = "test1",is_fuse = true)
    private HelloService helloService1;

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService2;

    @GetMapping("/test1")
    public String test1(){
        return helloService1.sayHello(new Hello("111", "222"));
    }
    @GetMapping("/test2")
    public String test2(){
        return helloService2.sayHello(new Hello("111", "222"));
    }

   public String handleHelloService1Exception(){
        return "handleHelloService1Exception";
   }

//    public static void main(String[] args) {
//        HelloController helloController = new HelloController();
//        try {
//            System.out.println(helloController.test4());
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String test4() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Method handleHelloService1Exception = HelloController.class.getMethod("handleHelloService1Exception");
//        Object invoke = handleHelloService1Exception.invoke(this);
//        return (String) invoke;
//    }

    @GetMapping("/test3")
    public String test3(){
        AtomicReference<String> msg = new AtomicReference<>("old");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "test3";
        });

        completableFuture.whenComplete((r,t)->{
            msg.set(r);
        });
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return msg.get();
    }
}
