package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;
    //获取全部品牌
    public List<Brand> findAll(){
        return brandDao.selectByExample(null);
    }

    //分页对象
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //分页插件 Mybatis逆向工程一
        PageHelper.startPage(pageNum,pageSize);
        //分页对象
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(null);

        return new PageResult(p.getTotal(),p.getResult());
    }
    //分页对象
    @Override
    public PageResult search(Integer pageNum, Integer pageSize,Brand brand) {
        //分页插件 Mybatis逆向工程一
        PageHelper.startPage(pageNum,pageSize);

        BrandQuery brandQuery = new BrandQuery();//外对象
                                                   //内createCriteria子对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        //品牌名称
        if(null != brand.getName() && !"".equals(brand.getName().trim())){
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }  //品牌首
        if(null != brand.getFirstChar() && !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }


        //分页对象
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(brandQuery);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void delete(Long[] ids) {
        BrandQuery query = new BrandQuery();
        query.createCriteria().andIdIn(Arrays.asList(ids));
        brandDao.deleteByExample(query);


    }
    //查询所有品牌
    public List<Map> selectOptionList(){
       return brandDao.selectOptionList();
    }
}
