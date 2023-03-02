package com.acqz.rpc.registry;


import com.acqz.common.extension.SPI;

import java.net.InetSocketAddress;

/**
 * service registration
 *
 */
@SPI
public interface ServiceRegistry {
    /**
     * register service
     *
     * @param rpcServiceName    rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
