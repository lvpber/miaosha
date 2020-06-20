package com.lvpb.miaosha.service;

import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.redis.MiaoshaUserKey;
import com.lvpb.miaosha.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService
{
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private RedisOperator redisOperator;

    /** 用户登陆成功后，再次访问时会带相应的token，这时我们需要从缓存中获取 */
    // 一方面是获取token对应的用户信息，另一方面我们需要延长token的时效性
    public MiaoshaUser getByToken(HttpServletResponse response,String token)
    {
        if(StringUtils.isEmpty(token))
            return null;
        MiaoshaUser miaoshaUser = redisOperator.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if(miaoshaUser != null)
            addCookie(miaoshaUser,response,token);

        return miaoshaUser;
    }

    private void addCookie(MiaoshaUser miaoshaUser, HttpServletResponse response,String token)
    {
        /** key:value --- {tk:token : miaoshaUser} */
        redisOperator.set(MiaoshaUserKey.token,token,miaoshaUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        //设置Cookie的有效期为生成的token的有效期
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        //设置网站的根目录
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
