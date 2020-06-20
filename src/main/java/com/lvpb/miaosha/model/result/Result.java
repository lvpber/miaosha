package com.lvpb.miaosha.model.result;

import java.io.Serializable;

/**
 * 前端请求返回值的封装
 * code : 000
 * msg  : 请求成功
 * data : data 结果
 * @param <T>
 */
public class Result<T> implements Serializable
{
    private int code;
    private String msg;
    private T data;

    public Result(){}

    /**
     * 成功返回值的构造函数
     * @param data
     */
    private Result(T data)
    {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    /**
     * 失败返回值的构造函数
     * @param codeMsg
     */
    private Result(CodeMsg codeMsg)
    {
        if(codeMsg == null)
            return ;

        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    /**
     * 处理成功时候调用
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data)
    {
        return new Result<>(data);
    }

    /**
     * 请求失败时调用
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(CodeMsg codeMsg)
    {
        return new Result<>(codeMsg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

}
