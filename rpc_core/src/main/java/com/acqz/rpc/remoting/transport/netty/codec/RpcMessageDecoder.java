package com.acqz.rpc.remoting.transport.netty.codec;

import com.acqz.common.enums.CompressTypeEnum;
import com.acqz.common.enums.SerializationTypeEnum;
import com.acqz.common.extension.ExtensionLoader;
import com.acqz.rpc.compress.Compress;
import com.acqz.rpc.remoting.constants.RpcConstants;
import com.acqz.rpc.remoting.dto.RpcMessage;
import com.acqz.rpc.remoting.dto.RpcRequest;
import com.acqz.rpc.remoting.dto.RpcResponse;
import com.acqz.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author haofeng
 * @date 2023/2/25 12:13
 * @description 解码
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: 长度字段的偏移量
        // lengthFieldLength: 长度字段的长度
        // lengthAdjustment: 要添加到长度字段值的补偿值 --- 长度字段的值是整个消息的长度，而我们需要除此之外的长度，就需要一个负数作为补偿
        // initialBytesToStrip: 要剥离的字节数
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      最大的帧长度，若超出则数据会被丢弃
     * @param lengthFieldOffset   字段偏移量，读取时跳过指定字节长度的字段
     * @param lengthFieldLength   字段中的字节数
     * @param lengthAdjustment    要添加到 length字段的补偿值
     * @param initialBytesToStrip 是否要跳过头部信息，直接读取正文
                                  如果你需要接收所有的头部+主体数据，这个值是0
                                  如果你只想接收正文数据，那么你需要跳过头部消耗的字节数。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return null;
    }

    private Object decodeFrame(ByteBuf frame) {
        checkMagicNumber(frame);
        checkVersion(frame);
        int fullLength = frame.readInt();
        byte messageType = frame.readByte();
        byte codecType = frame.readByte();
        byte compressType = frame.readByte();
        int requestId = frame.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int extLen = frame.readInt();
        Map extFields = null;
        if (extLen > 0){
            Serializer extension = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(SerializationTypeEnum.JSON.getName());
            byte[] bytes = new byte[extLen];
            frame.readBytes(bytes);
            extFields = extension.deserialize(bytes, Map.class);
        }
        if (extFields != null){
            rpcMessage.setExtensionFields(extFields);
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH - RpcConstants.EXT_LENGTH - extLen;
        if (bodyLength > 0){
            byte[] bytes = new byte[bodyLength];
            frame.readBytes(bytes);
            //解压
            String compressName = CompressTypeEnum.getName(compressType);
            if (compressName != null){
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bytes = compress.decompress(bytes);
            }
            //反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bytes, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else if (messageType == RpcConstants.RESPONSE_TYPE){
                RpcResponse tmpValue = serializer.deserialize(bytes, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf frame) {
        byte version = frame.readByte();
        //todo 初始化不同版本对应的消息处理器
        if (version != RpcConstants.VERSION){
            throw new IllegalArgumentException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf frame) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        frame.readBytes(tmp);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("Unknown magic code---->"+ tmp.toString());
            }
        }
    }
}
