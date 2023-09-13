package io.github.guangxian.gencontroller.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE) // 注解只在源码中保留
@Target(ElementType.TYPE)
public @interface GenController {
    String url() default "";;
    /**
     * 例子
     * 若name = testUser，则url = /test-user， controller类名 = TestUserController
     * @return
     */
    String name() default "";
    String packagePath() default "";
    String springDocTagName() default "";
    String springDocTagDescription() default "";
    String sdf() default "";

}
