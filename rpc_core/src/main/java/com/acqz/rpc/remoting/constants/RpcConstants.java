package com.acqz.rpc.remoting.constants;

/**
 * @author haofeng
 * @date 2023/2/25 11:54
 * @description rpc 常量
 */

public class RpcConstants {
    public static final byte[] MAGIC_NUMBER = {(byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd'};

    //version information
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
}
