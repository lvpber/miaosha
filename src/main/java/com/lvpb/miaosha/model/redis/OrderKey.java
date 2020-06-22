package com.lvpb.miaosha.model.redis;

public class OrderKey extends BasePrefix
{
    public OrderKey(int expireSecond, String prefix) {
        super(expireSecond, prefix);
    }

    public OrderKey(String prefix){super(prefix);}

    // Orderkey:moug{gid}_{uid} 根据UserId和GoodsId查询秒杀订单信息 秒杀成功后 用于判断是否重复消费
    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");
}
