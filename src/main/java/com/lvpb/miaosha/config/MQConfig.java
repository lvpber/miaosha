package com.lvpb.miaosha.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *  消息会先发送到交换机，由交换机根据路由将消息发送到指定的队列上
 */

@Configuration
public class MQConfig
{
    // Queue
    public static final String MIAOSHA_QUEUE = "miaosha.queue";         // 这个队列是我们真实用到的，用于秒杀的时候将订单信息入队
    public static final String QUEUE = "queue";                         // Default Queue
    public static final String TOPIC_QUEUE1 = "topic.queue1";           // TopicQueue
    public static final String TOPIC_QUEUE2 = "topic.queue2";           // TopicQueue
    public static final String HEADERS_QUEUE = "headers.queue";         // HeadersQueue
    // Exchange
    public static final String TOPIC_EXCHANGE = "topicExchange";        // 消息模式
    public static final String FANOUT_EXCHANGE = "fanoutExchange";      // 广播模式
    public static final String HEADERS_EXCHANGE = "headersExchange";    // Header模式
    // RoutingKey
    public static final String ROUTING_KEY1 = "topic.key1";
    public static final String ROUTING_KEY2 = "topic.#";     //支持通配符， # 代表0个或多个


    // Direct模式
    @Bean
    public Queue queue()
    {
        //消息队列 队列名称，是否需要持久化
        return new Queue(QUEUE,true);
    }


    /**
     *  搞了两个topicqueue
     *  搞了一个交换机
     *  把交换机和指定的queue做了一个绑定
     *  到时候数据直接发送给交换机，交换机来负责将数据重定向到queue
     */
    // 两个队列 用于演示消息订阅模式和广播模式
    @Bean
    public Queue topicQueue1()
    {
        return new Queue(TOPIC_QUEUE1,true);
    }

    @Bean
    public Queue topicQueue2()
    {
        return new Queue(TOPIC_QUEUE2,true);
    }

    // 用于演示Header模式
    @Bean
    public Queue headersQueue()
    {
        return new Queue(HEADERS_QUEUE,true);
    }

    // Topic 模式
    // 消息会发送到exchange上，然后路由到指定的队列上，我们需要做一个绑定
    @Bean
    public TopicExchange topicExchange()
    {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1()
    {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTING_KEY1);   //topic.key1
    }

    @Bean
    public Binding topicBinding2()
    {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTING_KEY2);   //topic.#
    }

    // Fanout 广播模式
    @Bean
    public FanoutExchange fanoutExchange()
    {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding FanoutBinding1()
    {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding FanoutBinding2()
    {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }

    //Header模式
    @Bean
    public HeadersExchange headersExchange()
    {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Binding headerBinding()
    {
        Map<String,Object> map = new HashMap<>();
        map.put("header1","value1");
        map.put("header2","value2");
        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
    }

}
