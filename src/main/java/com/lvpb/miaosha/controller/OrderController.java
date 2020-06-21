package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.GoodsService;
import com.lvpb.miaosha.service.OrderService;
import com.lvpb.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/order")
public class OrderController
{
    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping(value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> detail(MiaoshaUser miaoshaUser,
                                        @RequestParam("orderId") long orderId)
    {
        // 重复劳作加一个拦截器 所有这种的url加一个@NeedLogin拦截器
        if(miaoshaUser == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        OrderInfo orderInfo = orderService.selectByPrimaryKey(orderId);

        //订单不存在
        if(orderInfo == null)
            return Result.error(CodeMsg.ORDER_NOT_EXIST);

        long goodsId = orderInfo.getGoodsId();
        Goods goods = goodsService.selectByPrimaryKey(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goods);
        orderDetailVo.setOrderInfo(orderInfo);

        return Result.success(orderDetailVo);
    }
}
