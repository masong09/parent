package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类管理
 */
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;

    //通过父Id查询商品分类结果集
    @RequestMapping("/findByParentId")
    public List<ItemCat> findItemCatListByParentId(Long parentId){

        return itemCatService.findItemCatListByParentId(parentId);
    }
}
