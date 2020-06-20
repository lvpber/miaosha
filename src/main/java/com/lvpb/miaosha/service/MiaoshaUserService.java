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

    public MiaoshaUser getByNickname(String nickname)
    {
        // 取缓存 { MiaoshaUserKey:nickname{nickname} } "类名" + ":" + "key逻辑名字" + "key值"
        MiaoshaUser miaoshaUser = redisOperator.get(MiaoshaUserKey.getByNickName,""+nickname,MiaoshaUser.class);
        if(miaoshaUser != null)
        {
            return miaoshaUser;
        }

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("nickname",nickname);
        List<MiaoshaUser> miaoshaUsers = miaoshaUserMapper.selectListByCon(hashMap);
        if(miaoshaUsers.size() <= 0)
            return null;

        miaoshaUser = miaoshaUsers.get(0);
        // 缓存
        redisOperator.set(MiaoshaUserKey.getByNickName,""+nickname,miaoshaUser);
        return miaoshaUser;
    }

    /**
     *
     * @param token     token 用于更新redis
     * @param nickname  手机号
     * @param formpass  修改后的密码
     * @return
     *
     * 为啥这里新创建一个对象去更新，而不是直接使用旧对象
     * 因为我们这里采用的方法是updateSelective，旧对象中所有的属性都可能有值，我们如果更新的话会全部更新，
     * 而更新的越多，效率就越差，对此我们新创建一个对象，只把要更新的内容放进去，然后局部更新就好了
     */
    public boolean updatePassword(String token,String nickname,String formpass)
    {
        // 获取旧的数据
        MiaoshaUser miaoshaUser = getByNickname(nickname);
        if(miaoshaUser == null)
            return false;

        // 更新数据库 局部更新
        MiaoshaUser miaoshaUserUpdate = new MiaoshaUser();
        miaoshaUserUpdate.setId(miaoshaUser.getId());
        miaoshaUserUpdate.setPassword(MD5Util.formPassToDBPass(formpass,miaoshaUser.getSalt()));
        miaoshaUserMapper.updateByPrimaryKeySelective(miaoshaUserUpdate);

        //清除缓存，所有该用户的信息都要被清除掉，该用户可能的所有缓存有两类：
        // 1. 用token标记的 这种不能直接删除，不然无法登录，应该更新redis中的缓存
        //    key : [ MiaoshaUserKey:tk{tokenValue} ]
        miaoshaUser.setPassword(miaoshaUserUpdate.getPassword());
        redisOperator.set(MiaoshaUserKey.token,token,miaoshaUser);
        // 2. 用nickname标记的
        //    key : [ MiaoshaUserkey:nickname{nickanameValue}]
        redisOperator.delete(MiaoshaUserKey.getByNickName,nickname);
        return true;
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
