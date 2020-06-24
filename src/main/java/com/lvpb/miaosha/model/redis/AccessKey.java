package com.lvpb.miaosha.model.redis;

public class AccessKey extends BasePrefix
{
    private AccessKey(int expireSeconds,String prefix)
    {
        super(expireSeconds,prefix);
    }

    /**
     *  key : Accesskey:access{path}_{uId}  Accesskey:access"路径"_"用户Id"
     *  value : count 访问次数
     */
    public static AccessKey access = new AccessKey(5,"access");
    public static AccessKey withExpire (int expire)
    {
        return new AccessKey(expire,"access");
    }

}
