package com.acqz.rpc.spring;

import com.acqz.common.extension.ExtensionLoader;
import com.acqz.common.factory.SingletonFactory;
import com.acqz.rpc.annotation.RpcReference;
import com.acqz.rpc.annotation.RpcService;
import com.acqz.rpc.config.RpcServiceConfig;
import com.acqz.rpc.provider.ServiceProvider;
import com.acqz.rpc.provider.impl.ZkServiceProviderImpl;
import com.acqz.rpc.proxy.RpcClientProxy;
import com.acqz.rpc.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * call this method before creating the bean to see if the class is annotated
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    /**
     * 初始化之前执行，把 服务类进行发布
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        if (aClass.isAnnotationPresent(RpcService.class)){
            RpcService rpcService = aClass.getAnnotation(RpcService.class);
            RpcServiceConfig serviceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(serviceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        //此类声明的所有字段
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(RpcReference.class)) {
                RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean,proxy);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
