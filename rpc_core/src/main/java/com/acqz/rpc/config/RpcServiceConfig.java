package com.acqz.rpc.config;

import com.acqz.rpc.remoting.fuse.CircuitBreaker;
import lombok.*;

/**
 * @author haofeng
 * @date 2023/2/25 14:28
 * @description 配置类用于服务注册
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * Method called when fuse occurs,throws an exception when null
     */
    private Object fallback;

    private Boolean is_fuse;

    private CircuitBreaker circuitBreaker;

    /**
     * target service
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + "_" + this.getGroup() + "_" + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
