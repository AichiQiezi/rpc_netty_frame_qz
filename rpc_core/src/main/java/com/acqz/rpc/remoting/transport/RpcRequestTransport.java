package com.acqz.rpc.remoting.transport;


import com.acqz.common.extension.SPI;
import com.acqz.rpc.remoting.dto.RpcRequest;

/**
 * send RpcRequest。
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
