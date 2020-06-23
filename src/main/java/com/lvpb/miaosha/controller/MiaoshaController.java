package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.rabbitmq.MiaoshaMessage;
import com.lvpb.miaosha.model.redis.GoodsKey;
import com.lvpb.miaosha.model.redis.MiaoshaKey;
import com.lvpb.miaosha.model.redis.OrderKey;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.GoodsService;
import com.lvpb.miaosha.service.MQSender;
import com.lvpb.miaosha.service.MiaoshaService;
import com.lvpb.miaosha.service.OrderService;
import com.lvpb.miaosha.utils.MD5Util;
import com.lvpb.miaosha.utils.RedisOperator;
import com.lvpb.miaosha.utils.UUIDUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private Map<Long,Boolean> localOvermap = new HashMap<>();

//    @RequestMapping(value = "/reset",method = RequestMethod.GET)
//    @ResponseBody
//    public Result<Boolean> reset()
//    {
//        List<Goods> goods = goodsService.selectAll();
//        for(Goods g : goods)
//        {
//            g.setStockCount(10);
//            redisOperator.set(GoodsKey.getMiaoshaGoodsStock,""+g.getId(),10);
//            localOvermap.put(g.getId(),false);
//            goodsService.updateSelective(g);
//        }
//        redisOperator.delete(OrderKey.getMiaoshaOrderByUidGid);
//        redisOperator.delete(MiaoshaKey.isGoodsOver);
//
//    }

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
            localOvermap.put(goodsTemp.getId(),false);
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
     *
     *  没有做接口隐藏之前的版本
     */
//    @RequestMapping(value = "do_miaosha",method = RequestMethod.POST)
//    @ResponseBody
//    public Result<Integer> do_miaosha(Model model, MiaoshaUser miaoshaUser,
//                                        @RequestParam("goodsId") long goodsId)
//    {
//        model.addAttribute("user",miaoshaUser);
//        if(miaoshaUser==null)
//        {
//            // 用户session失效
//            return Result.error(CodeMsg.SESSION_ERROR);
//        }
//
//        // 通过内存来减少redis的访问
//        boolean isOver = localOvermap.get(goodsId);
//        if(isOver)
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//
//        // 预减库存
//        long stock = redisOperator.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId,1L);
//        // 当前库存不足
//        if(stock <= 0)
//        {
//            localOvermap.put(goodsId,true);
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//
//        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
//        // 重复秒杀
//        if(miaoshaOrder != null)
//            return Result.error(CodeMsg.REPEAT_MIAOSHA);
//
//        // 入队 什么用户秒杀什么商品，入队信息[user,goodsid]
//        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
//        miaoshaMessage.setGoodsId(goodsId);
//        miaoshaMessage.setMiaoshaUser(miaoshaUser);
//
//        mqSender.sendMiaoshaMessage(miaoshaMessage);
//
//        /**
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
//         */
//        return Result.success(0);   // 表示排队中
//    }

    /**
     *  查询订单是否被秒杀掉 前端执行秒杀功能之后获取到的是一个临时结果，就是告诉前端给你申请做了，做没做成功不知道
     *  后续前端不断轮询访问这个结果
     *  @return  orderId : 成功, -1 : 秒杀失败 , 0 : 排队中
     */
    @RequestMapping(value = "result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser miaoshaUser,
                                      @RequestParam("goodsId") long goodsId)
    {
        // 用户没有登陆
        if(miaoshaUser == null)
        {
            System.out.println("用户没有登陆");
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = miaoshaService.getMiaoshaResult(miaoshaUser.getId(),goodsId);

        System.out.println("result = " + result);
        return Result.success(result);
    }

    /**
     *  访问地址隐藏
     *  访问需要加一个前缀，这个url的目的就是生成一个随机UUID，供前端使用
     */
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(Model model, MiaoshaUser miaoshaUser,
                                      @RequestParam("goodsId") long goodsId)
    {
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser==null)
        {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        String path  = miaoshaService.createMiaoshaPath(miaoshaUser,goodsId);

        return Result.success(path);
    }

    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> do_miaosha(Model model, MiaoshaUser miaoshaUser,
                                      @RequestParam("goodsId") long goodsId,
                                      @PathVariable("path") String path)
    {
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser==null)
        {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 验证path有没有问题 true没问题 false有问题
        boolean check = miaoshaService.checkPath(miaoshaUser,goodsId,path);
        if(!check)
        {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 通过内存来减少redis的访问
        boolean isOver = localOvermap.get(goodsId);
        if(isOver)
            return Result.error(CodeMsg.MIAO_SHA_OVER);

        // 预减库存
        long stock = redisOperator.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId,1L);
        // 当前库存不足
        if(stock <= 0)
        {
            localOvermap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,miaoshaUser.getId());
        // 重复秒杀
        if(miaoshaOrder != null)
            return Result.error(CodeMsg.REPEAT_MIAOSHA);

        // 入队 什么用户秒杀什么商品，入队信息[user,goodsid]
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setGoodsId(goodsId);
        miaoshaMessage.setMiaoshaUser(miaoshaUser);

        mqSender.sendMiaoshaMessage(miaoshaMessage);

        return Result.success(0);   // 表示排队中
    }

    /**
     *  生成图片验证码的接口
     */
    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> verifyCode(Model model, MiaoshaUser miaoshaUser,
                                         @RequestParam("goodsId") long goodsId)
    {
        model.addAttribute("user",miaoshaUser);
        if(miaoshaUser==null)
        {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage image = miaoshaService.createVerifyCode(miaoshaUser,goodsId);
        String path  = miaoshaService.createMiaoshaPath(miaoshaUser,goodsId);

        return Result.success(path);
    }

}
