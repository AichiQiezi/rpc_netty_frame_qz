package com.acqz.rpc.remoting.dto;

import lombok.*;

/**
 * @author haofeng
 * @date 2023/2/25 12:14
 * @description rpc消息
 */

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RpcMessage {
    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;
}
