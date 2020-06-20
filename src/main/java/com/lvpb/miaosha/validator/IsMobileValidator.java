package com.lvpb.miaosha.validator;

import com.lvpb.miaosha.utils.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile,String>
{

    private boolean required = false;

    /** 初始化方法，可以拿到注解的内容 */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        System.out.println("this is IsMobileValidator.initialize function");
        required = constraintAnnotation.required(); //拿到注解中的是否需要密码
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required)
        {
            //必须有手机号这个值
            return ValidatorUtil.isMobile(value);
        }
        else
        {
            if(value == null || value.length() == 0)
            {
                return true;
            }
            else
            {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
