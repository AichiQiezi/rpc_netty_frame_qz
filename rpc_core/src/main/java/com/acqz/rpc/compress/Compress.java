package com.acqz.rpc.compress;

import com.acqz.common.extension.SPI;

/**
 * @author haofeng
 * @date 2023/2/25 13:31
 * @description todo
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
