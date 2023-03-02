package com.acqz.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haofeng
 * @date 2023/2/25 13:26
 * @description 解压类型枚举
 */

@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
