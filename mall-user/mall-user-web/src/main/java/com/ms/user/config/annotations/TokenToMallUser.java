package com.ms.user.config.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface TokenToMallUser {

    String value() default "user";
}
