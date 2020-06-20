package com.lvpb.miaosha.config;

import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.service.MiaoshaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver
{
    @Autowired
    private MiaoshaUserService miaoshaUserService;

    /**
     * 第一个方法是一个判断，如果参数中有一个是MiaoshaUser的类的参数，就会做第二个函数处理
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    /**
     * 当上面方法为true时就会执行本方法
     * @param methodParameter
     * @param modelAndViewContainer
     * @param nativeWebRequest
     * @param webDataBinderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);

        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
        {
            System.out.println("没有cookie参数");
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(response,token);
//        System.out.println("this is aop and user is : " + miaoshaUser + "and the token is " + token);
        return miaoshaUser;
    }

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
}
