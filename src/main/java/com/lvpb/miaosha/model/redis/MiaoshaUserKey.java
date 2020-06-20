package com.lvpb.miaosha.model.redis;


public class MiaoshaUserKey extends BasePrefix
{
    //2天
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;
    public static MiaoshaUserKey getByNickName = new MiaoshaUserKey(0, "nickName");

    private MiaoshaUserKey(int expireSecond,String prefix)
    {
        super(expireSecond,prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");
}
