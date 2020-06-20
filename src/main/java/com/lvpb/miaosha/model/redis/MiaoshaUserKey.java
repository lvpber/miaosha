package com.lvpb.miaosha.model.redis;


public class MiaoshaUserKey extends BasePrefix
{
    //2天
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;


    private MiaoshaUserKey(int expireSecond,String prefix)
    {
        super(expireSecond,prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");
    //有效时间设置为0，表示永久缓存，一般对象缓存我们希望是永久的
    public static MiaoshaUserKey getByNickName = new MiaoshaUserKey(0, "nickName");
}
