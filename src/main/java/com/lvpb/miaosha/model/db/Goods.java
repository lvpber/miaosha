package com.lvpb.miaosha.model.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class Goods {
    private Long id;
    private String goodsName;
    private String goodsTitle;
    private String goodsImg;
    private BigDecimal goodsPrice;
    private Integer goodsStock;
    private String goodsDetail;
    private BigDecimal miaoshaPrice;
    private Integer stockCount;
    private Long startDate;
    private Long endTime;
}