package com.acqz.rpc.remoting.dto;

import lombok.*;

/**
 * @author haofeng
 * @date 2023/2/25 11:55
 * @description 封装发送给服务端的请求对象
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcRequest {
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + "_" + this.getGroup() + "_" + this.getVersion();
    }
}
