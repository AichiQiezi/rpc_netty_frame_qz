package com.acqz.rpc.remoting.transport.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author haofeng
 * @date 2023/3/7 17:38
 * @description 抽象 处理器，解决消息不同版本问题
 */

public abstract class AbstractRpcServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public abstract void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

}
