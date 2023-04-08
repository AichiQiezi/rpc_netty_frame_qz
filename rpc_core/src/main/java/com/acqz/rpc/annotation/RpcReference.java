package com.acqz.rpc.annotation;


import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

    /**
     * Whether to turn on the fuse
     * {@code true} yes
     */
    boolean is_fuse() default false;

    /**
     * Method called when fuse occurs,throws an exception when null
     * the function must be in the same class as the target class
     */
    String fallback() default "";

}
