package com.lvpb.miaosha.vo;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaUser;
import lombok.Getter;
import lombok.Setter;

/**
 *  goods/detail 返回给前端的信息，
 *  1. 商品信息
 *  2. 两个状态值
 *  3. 用户信息
 */
@Getter
@Setter
public class GoodsDetailVo
{
    /** 商品 */
    private Goods goods;
    /** 秒杀状态 */
    private int miaoshaStatus;
    /** 剩余时间 */
    private int remainSeconds;
    /** 秒杀用户信息 */
    private MiaoshaUser miaoshaUser;
}
