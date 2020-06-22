package com.lvpb.miaosha.utils;

import com.lvpb.miaosha.model.db.MiaoshaUser;
import com.lvpb.miaosha.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 这个类的作用是帮助我们生成5000个用户
 */
@Component
public class UserUtil
{
    @Autowired
    private MiaoshaUserService miaoshaUserService;

    public void createUser(int count) throws Exception{
        List<MiaoshaUser> users = new ArrayList(count);
        //生成用户插入数据库
        for(int i=0;i<count;i++) {
            MiaoshaUser user = new MiaoshaUser();
            user.setId(1L+i);
            user.setLoginCount(1);
            user.setNickname(String.valueOf(13000000000L+i));
            user.setRegisterDate(new Date().toString());
            user.setSalt("1a2b3c");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
            users.add(user);
        }
        System.out.println("insert to db , count : " + miaoshaUserService.insertBatch(users));


        //登录，生成token
        String urlString = "http://localhost:8001/login/create_token";

        File file = new File("D:/tokens.txt");
        if(file.exists()) {
            file.delete();
        }

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for(int i=0;i<users.size();i++) {
            MiaoshaUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection)url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile="+user.getNickname()+"&password="+MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0 ,len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
//			JSONObject jo = JSON.parseObject(response);
//			String token = jo.getString("data");
            System.out.println("create token : " + user.getNickname());

            String row = user.getNickname()+","+response;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getNickname());
        }
        raf.close();

        System.out.println("over");
    }

//    public static void main(String[] args)throws Exception {
//        createUser(3);
//    }
}
