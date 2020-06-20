package com.lvpb.miaosha.model.redis;

public class GoodsKey extends BasePrefix
{
    /**
     * 页面缓存的有效期一般较短，设置为60s
     */
    public GoodsKey(int expireSecond , String prefix) {
        super(expireSecond,prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");
}
