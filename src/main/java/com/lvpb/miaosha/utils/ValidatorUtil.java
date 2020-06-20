package com.lvpb.miaosha.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检验手机号是否有效
 */
public class ValidatorUtil
{
    public static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");
    public static boolean isMobile(String mobile)
    {
        System.out.println("this is ValidatorUtil and the mobile is " + mobile);
        if(StringUtils.isEmpty(mobile))
            return false;
        Matcher m = mobile_pattern.matcher(mobile);
        return m.matches();
    }

//    public static void main(String args[])
//    {
//        System.out.println(isMobile("1888888888"));
//    }
}
