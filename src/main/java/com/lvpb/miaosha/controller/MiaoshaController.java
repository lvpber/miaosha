package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.service.GoodsService;
import com.lvpb.miaosha.service.MiaoshaService;
import com.lvpb.miaosha.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping(value = "miaosha")
public class MiaoshaController
{
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    /**
     * 5000 * 10
     * QPS : 1091
     * */
    @RequestMapping(value = "do_miaosha",method = RequestMethod.POST)
    public String do_miaosha(Model model, MiaoshaUser miaoshaUser,
                             @RequestParam("goodsId") long goodsId)
    {
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser==null)
        {
            return "login";
        }

        //判断库存
        Goods goods = goodsService.selectByPrimaryKey(goodsId);
        int stockCount = goods.getStockCount();
        if(stockCount <= 0)
        {
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }

        //判断是否已经秒杀到了 防止一个人秒杀了多个商品 从订单中查
        //select from miaoshaorder where userid = ? and goodsId = ?
        List<MiaoshaOrder> miaoshaOrders = orderService.selectListByCon(goodsId,miaoshaUser.getId());
        if(miaoshaOrders.size() > 0)
        {
            //用户已经秒杀过了
            model.addAttribute("errmsg", CodeMsg.REPEAT_MIAOSHA.getMsg());
            return "miaosha_fail";
        }

        //减库存 -- 下订单 -- 写入秒杀订单 事务操作
        OrderInfo orderInfo =  miaoshaService.doMiaosha(miaoshaUser,goods);
        //成功秒杀或者失败秒杀，将订单和商品放进model
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);
        return "order_detail";
    }
}
