package com.lvpb.miaosha.model.redis;

public abstract class BasePrefix implements KeyPrefix
{
    private int expireSecond;       //过期时间，默认0永久不过期
    private String prefix;

    public BasePrefix(String prefix)
    {
        expireSecond = 0;
        this.prefix = prefix;
    }

    public BasePrefix(int expireSecond,String prefix)
    {
        this.expireSecond = expireSecond;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSecond;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
