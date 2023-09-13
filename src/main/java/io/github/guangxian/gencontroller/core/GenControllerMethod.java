package io.github.guangxian.gencontroller.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE) // 注解只在源码中保留
@Target(ElementType.METHOD)
public @interface GenControllerMethod {
//    String springDocSummary() default "";
//    String springDocDescription() default "";
    String springDocOperationSummary() default "";
    String springDocOperationDescription() default "";
    boolean ignore() default false;
}
