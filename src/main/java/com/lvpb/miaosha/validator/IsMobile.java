package com.lvpb.miaosha.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
/** 遇到这个注解去哪一个类进行验证 */
@Constraint(validatedBy = { IsMobileValidator.class })
public @interface IsMobile
{
    boolean required() default true;

    /** 如果校验不通过，应该返回什么信息 */
    String message() default "{手机号码格式有错误}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
