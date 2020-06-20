package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/goods")
public class GoodsController
{
    @Autowired
    private GoodsService goodsService;
    /**
     *  cookieValue 是前端发送给后端的，但是有时候，手机端不会通过cookie把用户信息发送过来
     *  这时候就需要通过明面上的参数token来获取，也就是说出现这样一种情况，先从cookie中获取，如果没有
     *  再从实参中获取，如果都没有，那就有问题了
     *  因此，cookieValue可能没有，对此加上required = false
     *
     */
    //下面的是用做对比的，日后学习时候再看看
//    @RequestMapping(value="/to_list", produces="text/html")
//    public String toList(HttpServletResponse response,Model model,
//                         @CookieValue(value = LoginService.COOKIE_NAME_TOKEN,required = false) String cookieToken,
//                         @RequestParam(value = LoginService.COOKIE_NAME_TOKEN,required = false) String paramToken)
//    {
//        //参数判断
//        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
//        {
//            return "login";
//        }
//        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//
//        /**
//         * 1. 从缓存中获取该用户的信息，key是发过来的cookie中的sessionid
//         * 2. 更新缓存中用户信息的过期时间(如果该用户存在的话)
//         */
//        MiaoshaUser user = miaoshaUserService.getByToken(response,token);
//        model.addAttribute("user",user);
//        return "goods_list";
//    }


    @RequestMapping(value="/to_list", produces="text/html")
    public String toList(Model model,MiaoshaUser miaoshaUser)
    {
        model.addAttribute("user",miaoshaUser);
        List<Goods> goods = goodsService.selectAll();
        model.addAttribute("goodsList",goods);
        return "goods_list";
    }


    @RequestMapping(value = "/all",method = RequestMethod.GET)
    @ResponseBody
    public List<Goods> selectAll()
    {
        return goodsService.selectAll();
    }

    @RequestMapping(value = "/selectById/{goodsId}",method = RequestMethod.GET)
    @ResponseBody
    public Goods selectByPrimaryKey(@PathVariable("goodsId") Long goodsId)
    {
        return goodsService.selectByPrimaryKey(goodsId);
    }

    @RequestMapping(value="/to_detail/{goodsId}", produces="text/html", method = RequestMethod.GET)
    public String toDetail(Model model, MiaoshaUser miaoshaUser,
                           @PathVariable("goodsId") long goodsId)
    {
        model.addAttribute("user",miaoshaUser);
        Goods goods = goodsService.selectByPrimaryKey(goodsId);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndTime().getTime();

        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if(now < startAt)
        {
            //秒杀未开始 倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)(startAt - now)/1000;
        }
        else if(now > endAt)
        {
            //秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }
        else
        {
            //正在进行秒杀
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("goods",goods);
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        return "goods_detail";
    }
}
