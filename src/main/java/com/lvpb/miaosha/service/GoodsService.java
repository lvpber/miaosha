package com.lvpb.miaosha.service;

import com.lvpb.miaosha.mapper.rdb.GoodsMapper;
import com.lvpb.miaosha.mapper.rdb.MiaoshaGoodsMapper;
import com.lvpb.miaosha.model.db.Goods;
import com.lvpb.miaosha.model.db.MiaoshaGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Service
public class GoodsService
{
    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private MiaoshaGoodsMapper miaoshaGoodsMapper;

    public List<Goods> selectAll()
    {
        return goodsMapper.selectAll();
    }

    public Goods selectByPrimaryKey(Long goodsId)
    {
        return goodsMapper.selectByPrimaryKey(goodsId);
    }


    /**
     * 秒杀库存减1 如果剩余商品数量等于0 则秒杀失败，因为没有存货了
     * @param goods
     * @return
     */
    public boolean reduceStock(Goods goods)
    {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("goodsId",goods.getId());
        List<MiaoshaGoods> miaoshaGoodsList = miaoshaGoodsMapper.selectListByCon(hashMap);

        //没有找到商品
        if(miaoshaGoodsList.size() <= 0)
        {
            return false;
        }

        MiaoshaGoods miaoshaGoods = miaoshaGoodsList.get(0);
        if(miaoshaGoods.getStockCount() <= 0)
            return false;

        int ret = miaoshaGoodsMapper.reduceStock(miaoshaGoods.getGoodsId());
        System.out.println("更新为 " + ret);
        if(ret <= 0)
            return false;

        return true;
    }

    public int updateSelective(Goods goods)
    {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("goodsId",goods.getId());
        List<MiaoshaGoods> miaoshaGoods = miaoshaGoodsMapper.selectListByCon(hashMap);
        if(miaoshaGoods.size() > 0)
        {
            MiaoshaGoods miaoshaGoods1 = new MiaoshaGoods();
            miaoshaGoods1.setId(miaoshaGoods.get(0).getId());
            miaoshaGoods1.setStockCount(goods.getStockCount());
            return miaoshaGoodsMapper.updateByPrimaryKeySelective(miaoshaGoods1);
        }

        return 0;
    }
}
