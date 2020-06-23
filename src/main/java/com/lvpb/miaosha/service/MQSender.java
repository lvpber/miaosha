package com.lvpb.miaosha.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lvpb.miaosha.config.MQConfig;
import com.lvpb.miaosha.model.rabbitmq.MiaoshaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender
{
    @Autowired
    private AmqpTemplate amqpTemplate;

    private final Gson gson = new GsonBuilder().create();

    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage)
    {
        String msg = gson.toJson(miaoshaMessage);
        log.info("send miaoshaMessage : " + miaoshaMessage);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }





    //注释代码仅供学习，在本项目中无实意
//    // 向消息队列发送数据
//    public void send(Object message)
//    {
//        String msg = gson.toJson(message);
//        log.info("send msg : " + msg);
//        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
//    }
//
//    public void sendTopic(Object message)
//    {
//        String msg = gson.toJson(message);
//        log.info("send msg to topicQueue : " + msg);
//        // 发送给哪一个exchange的哪一种主题的消息 { TopicExchange , RoutingKey , Message }
//        // RoutingKey2 包含了RoutingKey1，所以发送给RoutingKey1的消息也会发送给RoutingKey2，但是反之不一定
//        // 下面的例子将会出现这样一种现象，msg+“1”会发送给queue1 和 queue2 ，而msg+"2"只会发送给queue2
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg + "1");
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,msg + "2");
//    }
//
//    public void sendFanout(Object message)
//    {
//        String msg = gson.toJson(message);
//        log.info("send msg to topicQueue : " + msg);
//        // 发送给哪一个exchange的哪一种主题的消息 { TopicExchange , RoutingKey , Message }
//        // Fanout模式，将会把绑定的所有队列全部发送
//        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg + "1");
//    }
//
//    public void sendHeaders(Object message)
//    {
//        String msg = gson.toJson(message);
//        log.info("send msg to headerQueue : " + msg);
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setHeader("header1","value1");
//        messageProperties.setHeader("header2","value2");
//        Message realMsg = new Message(msg.getBytes(),messageProperties);
//        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",realMsg);
//    }

}
