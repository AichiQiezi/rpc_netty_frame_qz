package com.acqz.rpc.config;

import lombok.*;

/**
 * @author haofeng
 * @date 2023/2/25 14:28
 * @description 配置类
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
     * target service
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
