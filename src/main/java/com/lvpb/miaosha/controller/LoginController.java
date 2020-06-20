package com.lvpb.miaosha.controller;

import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import com.lvpb.miaosha.service.LoginService;
import com.lvpb.miaosha.utils.ValidatorUtil;
import com.lvpb.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping(value = "/login")
public class LoginController
{
    @Autowired
    private LoginService loginService;

    @RequestMapping("/to_login")
    public String toLogin()
    {
        System.out.println("hellow");
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo)
    {
        System.out.println("fuck you baby this is do_login");
        System.out.println(loginVo);
        //参数校验
//        String passInput = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if(passInput == null || passInput.length() == 0)
//        {
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if(mobile == null || mobile.length() == 0)
//        {
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        //判断手机号格式
//        if(!ValidatorUtil.isMobile(mobile))
//        {
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
//        System.out.println("this is Login Controller.login function");
        System.out.println(loginService.login(response,loginVo));
        return Result.success(true);
    }
}
