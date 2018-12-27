package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojogroup.GoodsVo;
import entity.PageResult;

public interface GoodsService {


    public PageResult search(Integer page, Integer rows, Goods goods);

    //添加商品
    public void add(GoodsVo vo);

    public void delete(Long[] ids);


    //查询一个对象
    public GoodsVo findOne(Long id);
    public void updateStatus(Long[] ids, String status);
}
