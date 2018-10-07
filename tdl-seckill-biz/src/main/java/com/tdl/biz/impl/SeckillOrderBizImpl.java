package com.tdl.biz.impl;

import com.tdl.biz.SeckillOrderBiz;
import com.tdl.common.enums.SeckillGoodsStatusEnum;
import com.tdl.seckill.dao.SeckillGoodsDoMapper;
import com.tdl.seckill.dao.SeckillOrderDoMapper;
import com.tdl.seckill.dos.SeckillGoodsDo;
import com.tdl.seckill.dos.SeckillGoodsDoExample;
import com.tdl.seckill.dos.SeckillOrderDo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Description.
 *
 * @author : CeaserWang
 * @version : 1.0
 * @since : 2018/10/6 18:47
 */

@Component
public class SeckillOrderBizImpl implements SeckillOrderBiz {
    @Autowired
    SeckillOrderDoMapper seckillOrderDoMapper;
    @Autowired
    SeckillGoodsDoMapper seckillGoodsDoMapper;

    @Autowired
    private RedisLimit redisLimit;

    @Override
    public void subStockAndPushToMQ(Long goodId, Long userId) {
        //1、校验库存
        SeckillGoodsDo seckillGoodsDo  = seckillGoodsDoMapper.selectByPrimaryKey(goodId);
        if(seckillGoodsDo.getSale().longValue()==seckillGoodsDo.getTotal().longValue()){
            throw new RuntimeException("库存不足");
        }
        //2、减库存
        seckillGoodsDo.setSale(seckillGoodsDo.getSale() + 1);
        int count =  seckillGoodsDoMapper.updateOptimistic(seckillGoodsDo);
        if(count <= 0){
            throw new RuntimeException("扣减库存失败");
        }

        //3、创建订单
        SeckillOrderDo seckillOrderDo = new SeckillOrderDo();
        seckillOrderDo.setGoodsId(seckillGoodsDo.getId());
        seckillOrderDo.setName(seckillGoodsDo.getName());
        seckillOrderDo.setUserId(userId);
        seckillOrderDoMapper.insert(seckillOrderDo);
    }

    @Override
    public List<SeckillGoodsDo> getSeckillGoods() {
        SeckillGoodsDoExample seckillGoodsDoExample = new SeckillGoodsDoExample();
        seckillGoodsDoExample.createCriteria().andStatusEqualTo(SeckillGoodsStatusEnum.enabled.getCode());
        return seckillGoodsDoMapper.selectByExample(seckillGoodsDoExample);
    }
}
