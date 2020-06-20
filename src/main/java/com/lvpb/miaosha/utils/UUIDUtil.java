package com.lvpb.miaosha.utils;

import java.util.UUID;

public class UUIDUtil
{
    public static String uuid()
    {
        /** 原生的uuid内存在'-' 我们希望把这个'-'去掉 */
        return UUID.randomUUID().toString().replace("-","");
    }
}
