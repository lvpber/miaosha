package com.lvpb.miaosha.exception;

import com.lvpb.miaosha.model.result.CodeMsg;
import com.lvpb.miaosha.model.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//切面
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler
{
    //会拦截exception的异常，如果要拦截特定的异常，就在后面写特定的异常
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e)
    {
        System.out.println("this is my own exception handler");
        e.printStackTrace();
        if(e instanceof BindException)
        {
            System.out.println("this is my own bindException handler");
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError objectError = errors.get(0);

            String msg = objectError.getDefaultMessage();
            Result result = Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
            System.out.println(result.getCode() + "  " + result.getMsg() + "  " + result.getData());
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }
        else if(e instanceof GlobalException)
        {
            GlobalException ex = (GlobalException)e;
            return Result.error(ex.getCodeMsg());
        }
        else
        {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
