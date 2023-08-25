package io.github.guangxian.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE) // 注解只在源码中保留
@Target(ElementType.TYPE)
public @interface GenControllerConfig {
    boolean enable() default true;
    String packagePath() default "";
    String responseType() default "";
    String returnExpression() default "";
}
