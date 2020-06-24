package com.lvpb.miaosha.service;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaOrder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.redis.MiaoshaKey;
import com.lvpb.miaosha.utils.MD5Util;
import com.lvpb.miaosha.utils.RedisOperator;
import com.lvpb.miaosha.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService
{
    //需要使用别人的dao时候，要引入对方的service来处理
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisOperator redisOperator;

    private static Logger log = LoggerFactory.getLogger(MiaoshaService.class);

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
            //减库存失败，说明商品已经卖完了，这时候做一个标记，告诉商品没了，将来在查询是否秒杀成功的时候可以通过读取该状态
            //如果没有库存，也没有生成订单，就说明秒杀失败
            setGoodsOver(goods.getId());
        }
        return null;
    }

    // 获取异步执行秒杀的结果
    public long getMiaoshaResult(long userId,long goodsId)
    {
        MiaoshaOrder miaoshaOrder = orderService.selectMOByGoodsIdAndUserId(goodsId,userId);
        // 成功 return orderId
        if(miaoshaOrder != null)
        {
            return miaoshaOrder.getOrderId();
        }
        else
        {
            boolean isOver = getGoodsOver(goodsId);
            // 失败 return -1
            if(isOver)
            {
                return -1L;
            }
            // 排队 return 0
            else
            {
                return 0;
            }
        }
    }

    // 设置商品卖完标志
    private void setGoodsOver(long goodsId)
    {
        redisOperator.set(MiaoshaKey.isGoodsOver,""+goodsId,true);
    }

    // 查询商品是否已经卖完了
    private boolean getGoodsOver(long goodsId)
    {
        return redisOperator.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }


    // 生成前端页面请求的path，用于地址隐藏
    public String createMiaoshaPath(MiaoshaUser miaoshaUser,long goodsId)
    {
        if(miaoshaUser == null || goodsId <= 0)
            return null;
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        /**
         * key : MiaoshaKey:mp{Uid_GId}
         * value : pathStr
         */
        redisOperator.set(MiaoshaKey.getMiaoshaPath,""+miaoshaUser.getId()+"_"+goodsId,str);
        return str;
    }

    // 查询前端的页面请求地址是否合法，用于做地址隐藏使用
    // false 不合法
    // true  合法
    public boolean checkPath(MiaoshaUser miaoshaUser,long goodsId,String path)
    {
        if(miaoshaUser == null || path == null)
            return false;

        String redisPath = redisOperator.get(MiaoshaKey.getMiaoshaPath,""+miaoshaUser.getId()+"_"+goodsId,String.class);

        return path.equals(redisPath);
    }

    // 生成图像验证码
    public BufferedImage createVerifyCode(MiaoshaUser miaoshaUser, long goodsId)
    {
        if(miaoshaUser == null || goodsId <= 0)
            return null;
        /** 定义宽度高度 */
        int width = 80;
        int height = 32;

        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();

        //把验证码存到redis中
        int rnd = calc(verifyCode);
        log.info("生成的公式 : " + verifyCode + " | 公式的结果 ： " + rnd);

        /**
         * key = MiaoshaKey:vc{uId_gId}
         * value = 验证码的正确结果
         * */
        redisOperator.set(MiaoshaKey.getMiaoshaVerifyCode, miaoshaUser.getId()+"_"+goodsId, rnd);
        //输出图片
        return image;
    }

    /**
     *  计算生成的表达式的结果
     * */
    private static int calc(String verifyCode)
    {
        try
        {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(verifyCode);
        }
        catch (Exception e)
        {
            return 0;
        }
    }

//    public static void main(String args[])
//    {
//        System.out.println(calc("3+8-1"));
//    }

    private static char[] ops = new char[]{'+','-','*'};

    /** 生成验证码
     *  随机 + - *
     * */
    private static String generateVerifyCode(Random rdm)
    {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];  // 0 1 2
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    /** 用来验证验证码结果，验证成功就返回正确的path */
    public boolean checkVerifyCode(MiaoshaUser miaoshaUser, long goodsId, int verifyCode)
    {
        if(miaoshaUser == null || goodsId <= 0)
            return false;
        Integer codeOld = redisOperator.get(MiaoshaKey.getMiaoshaVerifyCode,""+miaoshaUser.getId()+"_"+goodsId,Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0)
            return false;
        /** 从redis中删除，防止刷新还可以用 */
        redisOperator.delete(MiaoshaKey.getMiaoshaVerifyCode,""+miaoshaUser.getId()+"_"+goodsId);
        return true;
    }
}
