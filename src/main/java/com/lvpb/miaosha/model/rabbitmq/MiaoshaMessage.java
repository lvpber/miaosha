package com.lvpb.miaosha.model.rabbitmq;

import com.lvpb.miaosha.model.db.MiaoshaUser;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MiaoshaMessage
{
    private MiaoshaUser miaoshaUser;
    private long goodsId;
}
