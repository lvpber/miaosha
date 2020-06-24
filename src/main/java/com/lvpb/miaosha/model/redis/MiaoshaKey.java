package com.lvpb.miaosha.model.redis;

public class MiaoshaKey extends BasePrefix
{
    public MiaoshaKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }


    // 商品已经秒杀完毕，用于在判断是否在排队中
    // key = MiaoshaKey:go{goodsId}
    // value = boolean
    public static MiaoshaKey isGoodsOver = new MiaoshaKey(0,"go");


    // 现在做了秒杀的地址隐藏，需要在访问秒杀地址之前先获取对应的地址，做法是
    // 生成一个随机字符串path，下一次访问时将这个字符串带上，判断字符串的正确性就允许用户访问了，
    // 这个生成的字符串就放在redis中，方便日后查询
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60,"mp");

    /** 生成的验证码，后端将其写进redis，日后检验的时候从redis获取 */
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300,"vc");

}
