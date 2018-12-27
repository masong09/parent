package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 模板管理
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {


    @Autowired
    private TypeTemplateDao typeTemplateDao;


    @Autowired
    private RedisTemplate redisTemplate;


    //分页查询
    public PageResult search(Integer page, Integer rows,  TypeTemplate typeTemplate){
        PageHelper.startPage(page,rows);
        PageHelper.orderBy("id desc");
        Page<TypeTemplate> p  = (Page<TypeTemplate>) typeTemplateDao.selectByExample(null);


        //品牌列表和规格列表数据的redis缓存建立
        saveToRedis();
        return new PageResult(p.getTotal(),p.getResult());
    }


    private  List<TypeTemplate> findAll(){
        return typeTemplateDao.selectByExample(null);
    }


    /**
     * 缓存品牌和规格的数据
     */
    private void saveToRedis() {

        //1. 找到所有的模板数据
        List<TypeTemplate> all = findAll();

        //2.遍历模板数据

        //2.遍历模板数据
        System.out.println("品牌和规格缓存建立");
        for (TypeTemplate typeTemplate : all) {
            //3. 建立品牌列表的缓存 : 大key : brandList 小key; 类型模板id  value : 品牌列表
            String brandIds = typeTemplate.getBrandIds();
            List<Map> brandlist = JSON.parseArray(brandIds, Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandlist);

            //4. 建立规格列表的缓存 : 大key : specList 小key; 类型模板id  value : 规格列表(还得有规格选项)
            List<Map> specList = findBySpecList(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);

        }


    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Override
    public List<Map> findBySpecList(Long id) {

        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();

        List<Map> list = JSON.parseArray(specIds, Map.class);

        for (Map map : list) {
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo((long) (Integer) map.get("id"));
            map.put("options", specificationOptionDao.selectByExample(query));
        }

       /* 0:Map
        id:
        27 text:
        网络 options:list
        1:Map
        id:
        32 text:
        机身内存
        Object-- > Integer-- > Long*/

        return list;
    }
}
