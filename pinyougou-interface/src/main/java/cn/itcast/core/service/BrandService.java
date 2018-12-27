package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    //获取全部品牌
    public List<Brand> findAll();

    //获取分页对象
    public PageResult findPage(Integer pageNum, Integer pageSize);
    //获取分页对象
    public PageResult search(Integer pageNum, Integer pageSize,Brand brand);

    //添加
    public void add( Brand brand);
    //修改
    public void update( Brand brand);
    //一个品牌
    public Brand findOne(Long id);

    //删除
    public void delete(Long[] ids);

    //查询所有品牌
    public List<Map> selectOptionList();
}
