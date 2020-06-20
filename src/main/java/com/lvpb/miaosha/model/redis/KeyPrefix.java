package com.lvpb.miaosha.model.redis;

public interface KeyPrefix
{
    int expireSeconds();

    String getPrefix();
}
