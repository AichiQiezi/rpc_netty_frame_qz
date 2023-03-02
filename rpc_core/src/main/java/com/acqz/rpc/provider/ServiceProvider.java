package com.acqz.rpc.provider;

import com.acqz.rpc.config.RpcServiceConfig;

/**
 * @author haofeng
 * @date 2023/2/25 14:30
 * @description 服务提供者
 */

public interface ServiceProvider {
    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
