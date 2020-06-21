package com.lvpb.miaosha.service;

import com.lvpb.miaosha.mapper.rdb.MiaoshaOrderMapper;
import com.lvpb.miaosha.mapper.rdb.OrderInfoMapper;
import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
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

    public List<MiaoshaOrder> selectMOByGoodsIdAndUserId(long goodsId,long userId)
    {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userId",userId);
        hashMap.put("goodsId",goodsId);
        return miaoshaOrderMapper.selectListByCon(hashMap);
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

        return orderInfo;
    }

}
