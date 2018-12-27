package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 商品分类管理
 */
@Service
public class ItemCatServiceImpl implements  ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;
    @Override
    public List<ItemCat> findItemCatListByParentId(Long parentId) {
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);



        //每次经过查询都会执行缓存的建立的方法
        saveToRedis();

        return itemCatDao.selectByExample(itemCatQuery);
    }

    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }



    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }


    @Autowired
    private RedisTemplate redisTemplate;

    private void saveToRedis(){
        System.out.println("商品分类缓存的创建");
        List<ItemCat> itemCatList = findAll();
        for (ItemCat itemCat : itemCatList) {
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
        }


    }

}
