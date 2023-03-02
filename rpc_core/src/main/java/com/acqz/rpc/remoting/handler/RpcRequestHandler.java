package com.acqz.rpc.remoting.handler;

import com.acqz.common.exception.RpcException;
import com.acqz.common.factory.SingletonFactory;
import com.acqz.rpc.provider.ServiceProvider;
import com.acqz.rpc.provider.impl.ZkServiceProviderImpl;
import com.acqz.rpc.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author haofeng
 * @date 2023/2/27 9:03
 * @description rpc请求处理器
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 处理 rpcRequest请求
     * @param rpcRequest
     * @return 返回方法调用的结果
     */
    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }

    /**
     * 调用目标方法，并获取返回值
     * @param rpcRequest 客户端的 rpc请求
     * @param service 服务对象
     * @return 目标方法执行后的返回值
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());

        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }


}
