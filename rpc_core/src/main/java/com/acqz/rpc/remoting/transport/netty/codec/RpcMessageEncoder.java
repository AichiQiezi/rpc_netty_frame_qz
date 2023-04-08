package com.acqz.rpc.remoting.transport.netty.codec;

import com.acqz.common.enums.CompressTypeEnum;
import com.acqz.common.enums.SerializationTypeEnum;
import com.acqz.common.extension.ExtensionLoader;
import com.acqz.common.utils.StringUtil;
import com.acqz.rpc.compress.Compress;
import com.acqz.rpc.remoting.constants.RpcConstants;
import com.acqz.rpc.remoting.dto.RpcMessage;
import com.acqz.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 0     1     2     3     4        5     6   7    8      9          10      11        12    13   14   15   16
 * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+----+---+
 * |   magic   code        |version |   full length       |messageType| codec |compress|    RequestId       |
 * +-----------------------+--------+---------------------+-----------+-----------+-----------+-------------+
 * | Extended field length |           Extended field .....                                                   .......
 * +-----------------------+--------+---------------------+-----------+-----------+-----------+-------------+
 * |                                                                                                        |
 * |                                         body                                                           |
 * |                                                                                                        |
 * |                                        ... ...                                                         |
 * +--------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）  4B Extended field length （拓展字段的长度）
 *
 * @author haofeng
 * @date 2023/2/25 12:13
 * @description 自定义协议编码器
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            //为 full length 预留空间
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // 拓展字段解析
            Map<String, Object> extensionFields = rpcMessage.getExtensionFields();
            int extLen = 0;
            if (extensionFields != null && extensionFields.size() > 0) {
                byte[] extBytes = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(SerializationTypeEnum.JSON.getName())
                        .serialize(extensionFields);
                out.writeInt((extLen = extBytes.length));
                out.writeBytes(extBytes);
            } else {
                out.writeInt(0);
            }
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH + RpcConstants.EXT_LENGTH + extLen;
            //心跳信息没有 消息正文等信息
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            //去更新帧的长度
            int writerIndex = out.writerIndex();
            out.writerIndex(writerIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writerIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
