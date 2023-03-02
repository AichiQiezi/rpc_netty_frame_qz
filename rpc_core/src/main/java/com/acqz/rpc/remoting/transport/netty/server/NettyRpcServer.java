package com.acqz.rpc.remoting.transport.netty.server;

import com.acqz.common.factory.SingletonFactory;
import com.acqz.rpc.config.RpcServiceConfig;
import com.acqz.rpc.provider.ServiceProvider;
import com.acqz.rpc.provider.impl.ZkServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author haofeng
 * @date 2023/2/25 14:27
 * @description 服务器
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){

    }

}
