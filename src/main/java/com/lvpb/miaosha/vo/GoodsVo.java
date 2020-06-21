package com.lvpb.miaosha.vo;

import com.lvpb.miaosha.model.db.Goods;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GoodsVo extends Goods
{
    private Integer stockCount;
    private Long startDate;
    private Long endTime;
}
