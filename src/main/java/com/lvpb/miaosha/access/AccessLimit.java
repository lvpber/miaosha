package com.lvpb.miaosha.access;

/**
 * 接口限流注解
 * 限制用户访问某一个接口在规定时间内的最大访问次数以及是否需要登录
 */
public @interface AccessLimit
{
    int seconds();
    int maxCount();
    boolean needLogin() default true;   //默认需要登陆
}
