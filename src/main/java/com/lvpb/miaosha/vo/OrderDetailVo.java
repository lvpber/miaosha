package com.lvpb.miaosha.vo;

import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.OrderInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailVo
{
    private Goods goods;
    private OrderInfo orderInfo;
}
