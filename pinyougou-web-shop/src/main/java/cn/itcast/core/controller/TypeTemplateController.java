package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模板管理
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;



    //查询一个
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id) {

        return typeTemplateService.findOne(id);

    }


    //查询规格结果集
    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id) {

        return typeTemplateService.findBySpecList(id);
    }


}
