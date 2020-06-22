package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.redis.BasePrefix;
import com.lvpb.miaosha.model.redis.GoodsKey;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.GoodsService;
import com.lvpb.miaosha.utils.RedisOperator;
import com.lvpb.miaosha.vo.GoodsDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/goods")
public class GoodsController
{
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisOperator redisOperator;

    /** thymeleaf框架提供的手动渲染 */
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
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
    @ResponseBody
    public String toList(HttpServletRequest request,
                         HttpServletResponse response,
                         Model model,MiaoshaUser miaoshaUser)
    {
        model.addAttribute("user",miaoshaUser);

        /**
         * 如果redis中存在该页面的缓存，就直接从redis中获取缓存页面，返回
         */
        String html = redisOperator.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html))
        {
            return html;
        }

        List<Goods> goods = goodsService.selectAll();
        model.addAttribute("goodsList",goods);

        //手动渲染 入缓存
        html = viewResolverManual(request,response,model,
                GoodsKey.getGoodsList,
                "",
                "goods_list");

        return html;
    }

    //旧的没有优化的
//    @RequestMapping(value="/to_detail/{goodsId}", produces="text/html", method = RequestMethod.GET)
//    public String toDetail(HttpServletRequest request,
//                           HttpServletResponse response,
//                           Model model, MiaoshaUser miaoshaUser,
//                           @PathVariable("goodsId") long goodsId)
//    {
//        model.addAttribute("user",miaoshaUser);
//        Goods goods = goodsService.selectByPrimaryKey(goodsId);
//
//        long startAt = goods.getStartDate();
//        long endAt = goods.getEndTime();
//
//        long now = System.currentTimeMillis();
//
//        int miaoshaStatus = 0;
//        int remainSeconds = 0;
//
//        if(now < startAt)
//        {
//            //秒杀未开始 倒计时
//            miaoshaStatus = 0;
//            remainSeconds = (int)(startAt - now)/1000;
//        }
//        else if(now > endAt)
//        {
//            //秒杀结束
//            miaoshaStatus = 2;
//            remainSeconds = -1;
//        }
//        else
//        {
//            //正在进行秒杀
//            miaoshaStatus = 1;
//            remainSeconds = 0;
//        }
//
//        model.addAttribute("goods",goods);
//        model.addAttribute("miaoshaStatus",miaoshaStatus);
//        model.addAttribute("remainSeconds",remainSeconds);
//
//        return "goods_detail";
//    }


    /** URL 缓存 旧，没有采用前后端分离的技术，用于日后学习观看差别 */
    @RequestMapping(value="/to_detail/{goodsId}", produces="text/html", method = RequestMethod.GET)
    @ResponseBody
    public String toDetail(HttpServletRequest request,
                           HttpServletResponse response,
                           Model model, MiaoshaUser miaoshaUser,
                           @PathVariable("goodsId") long goodsId)
    {
        // 取缓存
        String html = redisOperator.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html))
        {
            return html;
        }

        //手动渲染
        model.addAttribute("user",miaoshaUser);
        Goods goods = goodsService.selectByPrimaryKey(goodsId);

        long startAt = goods.getStartDate();
        long endAt = goods.getEndTime();

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

        //手动渲染 入缓存
        html = viewResolverManual(request,response,model,
                                  GoodsKey.getGoodsDetail,
                             ""+goodsId,
                         "goods_detail");

        return html;
    }

    /** 前端静态页面通过ajax向后端发送数据请求，获取后端数据，前后端分离优化 */
    @RequestMapping(value="/detail/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<GoodsDetailVo> goodsDetail(MiaoshaUser miaoshaUser,@PathVariable("goodsId") long goodsId)
    {
        Goods goods = goodsService.selectByPrimaryKey(goodsId);

        long startAt = goods.getStartDate();
        long endAt = goods.getEndTime();

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

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setMiaoshaUser(miaoshaUser);
        Result result = Result.success(goodsDetailVo);
        return result;
    }


    /**
     * @Note  负责进行页面的手动渲染，并将渲染的结果返回，如果渲染成功，缓存到redis中
     * @param request
     * @param response
     * @param model
     * @param basePrefix    Redis中key的前缀
     * @param keys          Redis中页面缓存或URL缓存的key
     * @param template      渲染的页面模板名称
     * @return
     */
    private String viewResolverManual(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Model model,
                                      BasePrefix basePrefix,
                                      String keys,
                                      String template)
    {
        WebContext webContext = new WebContext(request,response,
                request.getServletContext(),
                request.getLocale(),
                model.asMap());
        //两个参数  { 模板名称（html页面名称） context（包含上面业务数据的完整上下文）  }
        String html = thymeleafViewResolver.getTemplateEngine().process(template,webContext);

        if(!StringUtils.isEmpty(html))
        {
            redisOperator.set(basePrefix,keys,html);
        }

        return html;
    }
}
