package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojogroup.SpecificationVo;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    //查询条件  分页对象
    public PageResult search(Integer page, Integer rows,  Specification specification);
    //添加规格表  一张数据  主键返回来
    //规格选项多条数据  外键
    public void add(SpecificationVo vo);
    //修改  规格表  规格选项表多
    public void update(SpecificationVo vo);
    //查询一个
    public SpecificationVo findOne(Long id);
    //查询所有品牌
    public List<Map> selectOptionList();
}
