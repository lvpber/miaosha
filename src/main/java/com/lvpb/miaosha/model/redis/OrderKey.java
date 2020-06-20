package com.lvpb.miaosha.model.redis;

public class OrderKey extends BasePrefix
{
    public OrderKey(int expireSecond, String prefix) {
        super(expireSecond, prefix);
    }
}
