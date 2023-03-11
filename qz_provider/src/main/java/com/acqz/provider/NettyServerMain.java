package com.acqz.provider;


import com.acqz.common.service.HelloService;
import com.acqz.rpc.annotation.RpcScan;
import com.acqz.rpc.config.RpcServiceConfig;
import com.acqz.rpc.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 */
@RpcScan(basePackage = {"com.acqz"})
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // Register service manually
//        HelloService helloService2 = new HelloServiceImpl2();
//        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
//                .group("test2").version("version2").service(helloService2).build();
//        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
