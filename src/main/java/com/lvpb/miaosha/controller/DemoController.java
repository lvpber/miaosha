package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.mapper.rdb.MiaoshaUserMapper;
import com.lvpb.miaosha.mapper.rdb.OrderInfoMapper;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.db.OrderInfo;
import com.lvpb.miaosha.model.redis.UserKey;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.model.result.User;
import com.lvpb.miaosha.utils.RedisOperator;
import com.lvpb.miaosha.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/test")
public class DemoController
{
    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private MiaoshaUserMapper miaoshaUserMapper;

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public void testRedisObj() {
        User user = new User();
        user.setAge(1);
        user.setId(1);
        user.setName("fuck xiaoming");
        redisOperator.set(UserKey.getById,""+1,user);

        User userNew = redisOperator.get(UserKey.getById,"1",User.class);   //UserKey:id{id}
        System.out.println(userNew);
    }

    @RequestMapping("/ok")
    public String ok(){
        return "ok";
    }

    @RequestMapping("/hello")
    @ResponseBody
    String home()
    {
        return "hello";
    }


    @RequestMapping("/all")
    @ResponseBody
    public List<MiaoshaUser> all()
    {
        return miaoshaUserMapper.selectAll();
    }

    @RequestMapping(value = "/selectById/{mobile}" , method = RequestMethod.GET)
    @ResponseBody
    public MiaoshaUser selectById(@PathVariable("mobile") long mobile)
    {
        return miaoshaUserMapper.selectByPrimaryKey(mobile);
    }

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @RequestMapping(value = "/insertOrder", method = RequestMethod.POST)
    public void Insert()
    {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(111L);
        orderInfo.setGoodsName("fuck");
        orderInfo.setGoodsPrice(new BigDecimal(0.01));
        orderInfo.setOrderChannel((byte) 1);
        orderInfo.setStatus((byte) 0);
        orderInfo.setUserId(1L);
        orderInfoMapper.insert(orderInfo);

        System.out.println(orderInfo.getId());
    }

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser miaoshaUser)
    {
        return Result.success(miaoshaUser);
    }


    @Autowired
    private UserUtil userUtil;

    @RequestMapping("/addUser")
    @ResponseBody
    public String insertUser() throws Exception
    {
        userUtil.createUser(5000);
        return "success";
    }
}
