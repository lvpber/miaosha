package com.lvpb.miaosha.service;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MiaoshaService
{
    //需要使用别人的dao时候，要引入对方的service来处理
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    //减库存 -- 下订单 -- 写入秒杀订单 这是一个事务操作，所以要放到一个方法中执行
    @Transactional
    public OrderInfo doMiaosha(MiaoshaUser miaoshaUser, Goods goods)
    {
        /** 减库存，秒杀的库存 */
        boolean success = goodsService.reduceStock(goods);
        if(success)
        {
            //减库存成功
            /** 下订单 order_info miaosha_order*/
            OrderInfo orderInfo = orderService.createOrder(miaoshaUser,goods);
            return orderInfo;
        }
        else
        {

        }
        return null;
    }
}
