package com.acqz.rpc.remoting.transport.netty.server;

import com.acqz.common.enums.CompressTypeEnum;
import com.acqz.common.enums.RpcResponseCodeEnum;
import com.acqz.common.enums.SerializationTypeEnum;
import com.acqz.common.factory.SingletonFactory;
import com.acqz.rpc.remoting.constants.RpcConstants;
import com.acqz.rpc.remoting.dto.RpcMessage;
import com.acqz.rpc.remoting.dto.RpcRequest;
import com.acqz.rpc.remoting.dto.RpcResponse;
import com.acqz.rpc.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author haofeng
 * @date 2023/2/27 9:15
 * @description 服务端入站处理器
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(){
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage){
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    //执行目标方法并接收返回值
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> success = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(success);
                    }else {
                        RpcResponse<Object> fail = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(fail);
                        log.error("not writable now, message dropped");
                    }
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                }
            }
        }finally {
            //手动释放 ByteBuf，否则会造成内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }
}
