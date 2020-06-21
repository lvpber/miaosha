package com.lvpb.miaosha.model.db;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MiaoshaGoods {
    private Long id;
    private Long goodsId;
    private BigDecimal miaoshaPrice;
    private Integer stockCount;
    private Long startDate;
    private Long endTime;
}