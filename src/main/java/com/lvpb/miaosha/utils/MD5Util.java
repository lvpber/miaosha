package com.lvpb.miaosha.utils;



import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util
{
    private static final String salt = "1a2b3c4d";

    public static String md5(String src)
    {
        return DigestUtils.md5Hex(src);
    }

    //前端输入的密码，进行MD5加密的算法
    public static  String inputPassFormPass ( String  inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2)+ inputPass + salt.charAt(5) + salt.charAt(4) ;
        return md5(str);
    }

    //后端接收到密码然后加密写进数据库
    public static  String formPassToDBPass ( String  formPass ,String salt ) {
        String str = "" + salt.charAt(0) + salt.charAt(2)+ formPass + salt.charAt(5) + salt.charAt(4) ;
        return md5(str);
    }

    public static  String inputPassToDBPass ( String  inputPass ,String saltDB ) {
        String formPass = inputPassFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass ,saltDB ) ;
        return dbPass ;
    }

    public static void main(String args[])
    {
        System.out.println(inputPassToDBPass("b7797cce01b4b131b433b6acf4add449","1a2b3c4d"));
        //b7797cce01b4b131b433b6acf4add449
    }

}
