package com.lvpb.miaosha.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lvpb.miaosha.config.MQConfig;
import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.rabbitmq.MiaoshaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver
{
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    private final Gson gson = new GsonBuilder().create();

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message)
    {
        log.info("receive msg : " + message);
        MiaoshaMessage miaoshaMessage = gson.fromJson(message,MiaoshaMessage.class);

        MiaoshaUser miaoshaUser = miaoshaMessage.getMiaoshaUser();
        long goodsId = miaoshaMessage.getGoodsId();

        //判断库存 现在访问数据库 效率低
        Goods goods = goodsService.selectByPrimaryKey(goodsId);
        int stockCount = goods.getStockCount();
        if(stockCount <= 0)
        {
            //秒杀已经结束
            return;
        }

        //判断是否已经秒杀到了 防止一个人秒杀了多个商品 从订单中查 现在已经用redis来查询了，效率可以
        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
        if(miaoshaOrder != null)
        {
            //用户已经秒杀过了
            return;
        }

        //减库存 -- 下订单 -- 写入秒杀订单 事务操作 操作数据库
        miaoshaService.doMiaosha(miaoshaUser,goods);
        return;
    }

    // 下面代码供学习rabbitmq接收数据
//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receive(String message)
//    {
//        log.info("receive msg : " + message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receiveTopic1(String message)
//    {
//        log.info("receive topic queue1 msg : " + message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receiveTopic2(String message)
//    {
//        log.info("receive topic queue2 msg : " + message);
//    }
//
//    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
//    public void receiveHeaders(byte[] message)
//    {
//        log.info("receive headers queue msg : " + new String(message));
//    }
}
