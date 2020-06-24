package com.lvpb.miaosha.access;

import com.lvpb.miaosha.model.db.MiaoshaUser;

/**
 *  多线程环境下实现线程安全
 * */
public class UserContext
{
    private static ThreadLocal<MiaoshaUser> miaoshaUserThreadLocal = new ThreadLocal<>();

    public static void setUser(MiaoshaUser miaoshaUser)
    {
        miaoshaUserThreadLocal.set(miaoshaUser);
    }

    public static MiaoshaUser getUser()
    {
        return miaoshaUserThreadLocal.get();
    }
}
