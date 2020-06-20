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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@Service
public class MiaoshaUserService
{
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private MiaoshaUserMapper miaoshaUserMapper;

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

    public int insertBatch(List<MiaoshaUser> miaoshaUserList)
    {
        return miaoshaUserMapper.insertBatch(miaoshaUserList);
    }

    /** 根据用户信息创建token */
    public String createToken(HttpServletResponse response , LoginVo loginVo)
    {
        /** 登陆信息为空 */
        if(loginVo ==null){
            throw  new GlobalException(CodeMsg.SERVER_ERROR);
        }

        /** 根据登录信息获取用户信息 */
        String mobile =loginVo.getMobile();
        String password =loginVo.getPassword();

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("nickname",mobile);
        List<MiaoshaUser> miaoshaUsers = miaoshaUserMapper.selectListByCon(hashMap);

        if(miaoshaUsers.size() == 0)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        MiaoshaUser user = miaoshaUsers.get(0);


        String dbPass = user.getPassword();
        String saltDb = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(password,saltDb);
        if(!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        //生成cookie 将session返回游览器 分布式session
        String token= UUIDUtil.uuid();
        addCookie(user,response, token);
        return token ;
    }
}
