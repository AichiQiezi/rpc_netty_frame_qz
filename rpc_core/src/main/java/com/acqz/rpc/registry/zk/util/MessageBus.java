package com.acqz.rpc.registry.zk.util;

import lombok.*;

import java.util.List;

/**
 * @author haofeng
 * @date 2023/4/7 12:21
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageBus {
    private long version;
    private String rpcServiceName;
    private List<String> serviceAddresses;
}
