package com.lvpb.miaosha.vo;

import com.lvpb.miaosha.model.db.Goods;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GoodsVo extends Goods
{
    private Integer stockCount;
    private Date startDate;
    private Date endTime;
}
