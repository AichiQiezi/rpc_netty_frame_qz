package com.acqz.rpc.proxy;

import com.acqz.common.enums.RpcErrorMessageEnum;
import com.acqz.common.enums.RpcResponseCodeEnum;
import com.acqz.common.exception.FuseException;
import com.acqz.common.exception.RpcException;
import com.acqz.common.factory.SingletonFactory;
import com.acqz.rpc.config.CustomThreadPool;
import com.acqz.rpc.config.RpcServiceConfig;
import com.acqz.rpc.remoting.dto.RpcRequest;
import com.acqz.rpc.remoting.dto.RpcResponse;
import com.acqz.rpc.remoting.fuse.AbstractCircuitBreaker;
import com.acqz.rpc.remoting.fuse.CircuitBreaker;
import com.acqz.rpc.remoting.fuse.ExceptionCircuitBreaker;
import com.acqz.rpc.remoting.fuse.RequestCounter;
import com.acqz.rpc.remoting.fuse.policy.ExponentialBackoffRetryPolicy;
import com.acqz.rpc.remoting.fuse.policy.RetryPolicy;
import com.acqz.rpc.remoting.transport.RpcRequestTransport;
import com.acqz.rpc.remoting.transport.socket.T;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private Object target;

    private static final String INTERFACE_NAME = "interfaceName";

    private final RetryPolicy retryPolicy = SingletonFactory.getInstance(ExponentialBackoffRetryPolicy.class);

    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;
    private final ExceptionCircuitBreaker circuitBreaker = SingletonFactory.getInstance(ExceptionCircuitBreaker.class);

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig,Object target) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
        this.target = target;
    }


    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        Object result = null;
        Boolean is_fuse = rpcServiceConfig.getIs_fuse();
        if (is_fuse != null && is_fuse) {
            RequestCounter requestCounter = circuitBreaker.getRequestCounter();
            log.info("request failed count->{}", requestCounter.getFailedCount());
            log.info("request total count->{}", requestCounter.getFailedCount());
            log.info("anomaly ratio->{}", requestCounter.getFailedCount() / requestCounter.getTotalCount());
                try {
                    result = doSendRpcRequestWithFallback(rpcRequest);
                }catch (FuseException e){
                    return handleFallback();
                }
        }else {
            result = doSendRpcRequest(rpcRequest);
        }
        return result;
    }

    private Object doSendRpcRequestWithFallback(RpcRequest rpcRequest) throws Throwable{
        if (!circuitBreaker.tryPass()){
            throw new FuseException();
        }
        return doSendRpcRequest(rpcRequest);
    }

    private Object handleFallback() throws Throwable {
        Method fallback = (Method) rpcServiceConfig.getFallback();
        if (fallback == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_FUSE);
        }
        return fallback.invoke(target);
    }

    private Object doSendRpcRequest(RpcRequest rpcRequest) throws Exception {
        RpcResponse<Object> rpcResponse = null;
        CompletableFuture<RpcResponse<Object>> completableFuture = null;
        completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        int attempt = 1;
        do {
            try {
                rpcResponse = completableFuture.get(3, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException e) {
                if (!retryPolicy.shouldRetry(attempt, e)) {
                    break;
                }
                attempt++;
                circuitBreaker.onRequestComplete(e);
                log.warn("the method call timed out...");
            }
        } while (true);
        check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        circuitBreaker.onRequestComplete(new Exception());
        if (rpcResponse == null) {
            RpcException rpcException = new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
            circuitBreaker.onRequestComplete(rpcException);
            throw rpcException;
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            RpcException rpcException = new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
            circuitBreaker.onRequestComplete(rpcException);
            throw rpcException;
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            RpcException rpcException = new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
            circuitBreaker.onRequestComplete(rpcException);
            throw rpcException;
        }
    }

}
