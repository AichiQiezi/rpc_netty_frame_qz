package com.acqz.rpc.remoting.dto;

import com.acqz.rpc.remoting.transport.netty.server.AbstractRpcServerHandler;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author haofeng
 * @date 2023/2/25 12:14
 * @description rpc message
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

    /**
     *  extension field
     */
    @Nullable
    private Map<String,Object> extensionFields;

    /**
     * handler for the message version
     */
    @Nullable
    private AbstractRpcServerHandler serverHandler;
}
