package cn.itcast.core.controller;


import cn.itcast.core.service.ItemsearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 搜索管理
 */
@RestController
@RequestMapping("/itemsearch")
public class ItemsearchController {

    @Reference
    private ItemsearchService itemsearchService;

    @RequestMapping("search")
    public Map<String,Object> search(@RequestBody Map<String,String> searchMap){

        Map<String, Object> search = itemsearchService.search(searchMap);

        return  search;

    }


}
