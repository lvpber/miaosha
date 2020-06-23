package com.lvpb.miaosha.model.redis;

public class GoodsKey extends BasePrefix
{
    /**
     * 页面缓存的有效期一般较短，设置为60s
     */
    public GoodsKey(int expireSecond , String prefix) {
        super(expireSecond,prefix);
    }

    //  商品列表信息
    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    //  商品详情信息
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");
    //  商品秒杀库存 0表示永远不失效
    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0,"gs");
}
