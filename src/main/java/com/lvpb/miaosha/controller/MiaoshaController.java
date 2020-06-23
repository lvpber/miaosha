package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.rabbitmq.MiaoshaMessage;
import com.lvpb.miaosha.model.redis.GoodsKey;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.GoodsService;
import com.lvpb.miaosha.service.MQSender;
import com.lvpb.miaosha.service.MiaoshaService;
import com.lvpb.miaosha.service.OrderService;
import com.lvpb.miaosha.utils.RedisOperator;
import com.sun.tools.javac.jvm.Code;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "miaosha")
public class MiaoshaController implements InitializingBean
{
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private MQSender mqSender;

    /**
     *  系统初始化该bean之后就会回调该接口，用于进行初始化操作
     * */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        List<Goods> goods = goodsService.selectAll();
        if(goods.size() == 0)
            return;

        //将库存信息加载到redis中
        for(Goods goodsTemp : goods)
        {
            // key = GoodsKey:gs{goodsId} , value = goodsStockCount
            redisOperator.set(GoodsKey.getMiaoshaGoodsStock,""+goodsTemp.getId(),goodsTemp.getStockCount());
        }
    }

    /**
     * 5000 * 10
     * QPS : 1091
     * */
    // 旧版本的秒杀，返回的是html，现在采用前后端分离技术实现，源代码保留用以将来做对比学习
//    @RequestMapping(value = "do_miaosha",method = RequestMethod.POST)
//    public String do_miaosha(Model model, MiaoshaUser miaoshaUser,
//                             @RequestParam("goodsId") long goodsId)
//    {
//        model.addAttribute("user",miaoshaUser);
//        if(miaoshaUser==null)
//        {
//            return "login";
//        }
//
//        //判断库存
//        Goods goods = goodsService.selectByPrimaryKey(goodsId);
//        int stockCount = goods.getStockCount();
//        if(stockCount <= 0)
//        {
//            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
//            return "miaosha_fail";
//        }
//
//        //判断是否已经秒杀到了 防止一个人秒杀了多个商品 从订单中查
//        //select from miaoshaorder where userid = ? and goodsId = ?
//        //List<MiaoshaOrder> miaoshaOrders = orderService.selectListByCon(goodsId,miaoshaUser.getId());
//        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
//        if(miaoshaOrder != null)
//        {
//            //用户已经秒杀过了
//            model.addAttribute("errmsg", CodeMsg.REPEAT_MIAOSHA.getMsg());
//            return "miaosha_fail";
//        }
//
//        //减库存 -- 下订单 -- 写入秒杀订单 事务操作
//        OrderInfo orderInfo =  miaoshaService.doMiaosha(miaoshaUser,goods);
//        //成功秒杀或者失败秒杀，将订单和商品放进model
//        model.addAttribute("orderInfo",orderInfo);
//        model.addAttribute("goods",goods);
//        return "order_detail";
//    }


    /** GET POST 区别
     *  GET  具有幂等性      搜索引擎会在页面上进行缓存，虽然没有执行，但是搜索引擎会自动执行
     *  POST 不具备幂等性
     *  前后端分离
     */
//    @RequestMapping(value = "do_miaosha",method = RequestMethod.POST)
//    @ResponseBody
//    public Result<OrderInfo> do_miaosha(Model model, MiaoshaUser miaoshaUser,
//                             @RequestParam("goodsId") long goodsId)
//    {
//        model.addAttribute("user",miaoshaUser);
//        if(miaoshaUser==null)
//        {
//            // 用户session失效
//            return Result.error(CodeMsg.SESSION_ERROR);
//        }
//
//        //判断库存 现在访问数据库 效率低
//        Goods goods = goodsService.selectByPrimaryKey(goodsId);
//        int stockCount = goods.getStockCount();
//        if(stockCount <= 0)
//        {
//            //秒杀已经结束
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//
//        //判断是否已经秒杀到了 防止一个人秒杀了多个商品 从订单中查 现在已经用redis来查询了，效率可以
//        //select from miaoshaorder where userid = ? and goodsId = ?
//        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
//        if(miaoshaOrder != null)
//        {
//            //用户已经秒杀过了
//            return Result.error(CodeMsg.REPEAT_MIAOSHA);
//        }
//
//        //减库存 -- 下订单 -- 写入秒杀订单 事务操作 操作数据库
//        OrderInfo orderInfo =  miaoshaService.doMiaosha(miaoshaUser,goods);
//        return Result.success(orderInfo);
//    }

    /**
     *  1. 将商品数量预加载到redis中
     *  2. 下单后减库存，从redis中减，如果有库存就进入第三步，否则返回失败
     *  3. 将请求写进rabbitmq中，并立即返回请求排队中
     *  4. 请求出队，执行
     *  5. 客户端不断轮询结果，确认是否正确执行
     */
    @RequestMapping(value = "do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> do_miaosha(Model model, MiaoshaUser miaoshaUser,
                                        @RequestParam("goodsId") long goodsId)
    {
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser==null)
        {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 预减库存
        long stock = redisOperator.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId,1L);
        // 当前库存不足
        if(stock <= 0)
            return Result.error(CodeMsg.MIAO_SHA_OVER);

        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
        // 重复秒杀
        if(miaoshaOrder != null)
            return Result.error(CodeMsg.REPEAT_MIAOSHA);

        // 入队 什么用户秒杀什么商品，入队信息[user,goodsid]
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setGoodsId(goodsId);
        miaoshaMessage.setMiaoshaUser(miaoshaUser);


        /**
        //判断库存 现在访问数据库 效率低
        Goods goods = goodsService.selectByPrimaryKey(goodsId);
        int stockCount = goods.getStockCount();
        if(stockCount <= 0)
        {
            //秒杀已经结束
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否已经秒杀到了 防止一个人秒杀了多个商品 从订单中查 现在已经用redis来查询了，效率可以
        //select from miaoshaorder where userid = ? and goodsId = ?
        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
        if(miaoshaOrder != null)
        {
            //用户已经秒杀过了
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }

        //减库存 -- 下订单 -- 写入秒杀订单 事务操作 操作数据库
        OrderInfo orderInfo =  miaoshaService.doMiaosha(miaoshaUser,goods);
        return Result.success(orderInfo);
         */
        return Result.success(0);   // 表示排队中
    }

    @RequestMapping(value = "result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Integer> miaoshaResult(Model model, MiaoshaUser miaoshaUser,
                                      @RequestParam("goodsId") long goodsId)
    {
        // 用户没有登陆
        if(miaoshaUser == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        
        return null;
    }

}
