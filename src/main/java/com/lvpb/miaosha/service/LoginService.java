package com.lvpb.miaosha.service;

import com.lvpb.miaosha.exception.GlobalException;
import com.lvpb.miaosha.mapper.rdb.MiaoshaUserMapper;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.model.redis.MiaoshaUserKey;
import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.utils.MD5Util;
import com.lvpb.miaosha.utils.RedisOperator;
import com.lvpb.miaosha.utils.UUIDUtil;
import com.lvpb.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@Service
public class LoginService
{
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private MiaoshaUserMapper miaoshaUserMapper;

    /** 用于存储用户信息 */
    @Autowired
    private RedisOperator redisOperator;

    public boolean login(HttpServletResponse response, LoginVo loginVo)
    {
        System.out.println("this is loginService.login function");

        if(loginVo == null)
            throw new GlobalException(CodeMsg.SERVER_ERROR);

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("nickname",mobile);
        List<MiaoshaUser> miaoshaUsers = miaoshaUserMapper.selectListByCon(hashMap);

        if(miaoshaUsers.size() == 0)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        MiaoshaUser miaoshaUser = miaoshaUsers.get(0);

        //验证密码
        String realPass = miaoshaUser.getPassword();
        String realSalt = miaoshaUser.getSalt();
        String calcPass = MD5Util.formPassToDBPass(password,realSalt);

        if(!calcPass.equals(realPass))
        {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

//        System.out.println("认证成功");
        /** 登陆成功生成一个token，日后将其改为jwt试试看 */
        String token = UUIDUtil.uuid();
        /** key:value --- {tk:token : miaoshaUser}  */
        redisOperator.set(MiaoshaUserKey.token,token,miaoshaUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        //设置Cookie的有效期为生成的token的有效期
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        //设置网站的根目录
        cookie.setPath("/");
        response.addCookie(cookie);
        return true;
    }

}
