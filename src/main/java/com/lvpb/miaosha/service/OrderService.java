package com.lvpb.miaosha.service;

import com.lvpb.miaosha.mapper.rdb.MiaoshaOrderMapper;
import com.lvpb.miaosha.mapper.rdb.OrderInfoMapper;
import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.redis.OrderKey;
import com.lvpb.miaosha.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderService
{
    @Autowired
    private MiaoshaOrderMapper miaoshaOrderMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    /** 缓存优化，将查询到的订单信息放置到缓存中，日后查询的时候可以直接从缓存中查询 */
    @Autowired
    private RedisOperator redisOperator;

    // 没有使用缓存 直接从数据查询是否已经重复消费 存在优化，可以放到redis中后面查询更快，函数保留的目的是为了日后对比学习
//    public MiaoshaOrder selectMOByGoodsIdAndUserId(long goodsId,long userId)
//    {
//        HashMap<String,Object> hashMap = new HashMap<>();
//        hashMap.put("userId",userId);
//        hashMap.put("goodsId",goodsId);
//        List<MiaoshaOrder> miaoshaOrders = miaoshaOrderMapper.selectListByCon(hashMap);
//
//        if(miaoshaOrders.size() > 0)
//            return miaoshaOrders.get(0);
//        return null;
//    }

    // 使用redis缓存优化
    public MiaoshaOrder selectMOByGoodsIdAndUserId(long goodsId,long userId)
    {
        return redisOperator.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
    }

    public OrderInfo selectByPrimaryKey(long orderId)
    {
        return orderInfoMapper.selectByPrimaryKey(orderId);
    }

    /** 下订单 order_info miaosha_order*/
    @Transactional
    public OrderInfo createOrder(MiaoshaUser miaoshaUser, Goods goods)
    {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel((byte) 1);
        orderInfo.setStatus((byte) 0);
        orderInfo.setUserId(miaoshaUser.getId());
        orderInfoMapper.insertSelective(orderInfo);

        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(miaoshaUser.getId());
        miaoshaOrderMapper.insertSelective(miaoshaOrder);

        // 生成订单之后写进redis中，用于日后进行缓存查询
        redisOperator.set(OrderKey.getMiaoshaOrderByUidGid,""+miaoshaUser.getId()+"_"+goods.getId(),miaoshaOrder);

        return orderInfo;
    }

}
