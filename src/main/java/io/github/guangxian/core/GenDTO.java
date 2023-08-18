package io.github.guangxian.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE) // 注解只在源码中保留
@Target(ElementType.TYPE)
public @interface GenDTO {
    String url() default "";;
    String tag() default "";
    String controller() default "";;

    /**
     * 例子
     * 若name = testUser，则url = /test-user， controller类名 = TestUserController
     * @return
     */
    String name() default "";
}
