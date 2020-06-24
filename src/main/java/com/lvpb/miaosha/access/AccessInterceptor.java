package com.lvpb.miaosha.access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.redis.AccessKey;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.MiaoshaService;
import com.lvpb.miaosha.service.MiaoshaUserService;
import com.lvpb.miaosha.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 *  实现拦截器
 * */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter
{
    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @Autowired
    private RedisOperator redisOperator;

    private final Gson gson = new GsonBuilder().create();

    private static Logger log = LoggerFactory.getLogger(AccessInterceptor.class);

    // 方法执行之前拦截，处理
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if(handler instanceof HandlerMethod)
        {
            // 获取当前用户信息,放置在一个ThreadLocal中
            MiaoshaUser miaoshaUser = getMiaoshaUser(request,response);
            UserContext.setUser(miaoshaUser);


            // 尝试拿到方法的注解
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null)
            {
                return true;    //没有限制
            }


            // 如果方法存在该注解，拿到注解中的参数信息
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            log.info("seconds = " + seconds + ",maxCount = " + maxCount + ",needLogin = "+ needLogin);
            String key = request.getRequestURI();
            if(needLogin)
            {
                if(miaoshaUser == null)
                {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key = key + "_" + miaoshaUser.getId();
            }
            else
            {
                // do nothing
            }

            // martine flower 重构-改善既有代码的设计
            AccessKey accessKey = AccessKey.withExpire(seconds);
            Integer count = redisOperator.get(accessKey,key,Integer.class);
            log.info("count = " + count);
            if(count == null)
            {
                redisOperator.set(accessKey,key,1);
            }
            else if(count < maxCount)
            {
                redisOperator.incr(accessKey,key,1);
            }
            else
            {
                render(response,CodeMsg.ACCESS_LIMIT);
                return false;
            }

        }
        return true;
    }

    // 获取用户信息
    private MiaoshaUser getMiaoshaUser(HttpServletRequest request,HttpServletResponse response)
    {
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);

        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
        {
            System.out.println("没有cookie参数");
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(response,token);
        return miaoshaUser;
    }

    // 辅助函数
    private String getCookieValue(HttpServletRequest request, String cookieNameToken)
    {
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0)
            return null;
        for(Cookie cookie:cookies)
        {
            if(cookie.getName().equals(cookieNameToken))
            {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void render(HttpServletResponse response, CodeMsg codeMsg) throws Exception
    {
        OutputStream outputStream = response.getOutputStream();
        String str = gson.toJson(codeMsg);
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }
}
