package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojogroup.SpecificationVo;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;
    //查询条件  分页对象
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }

    //查询一个
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }
    //添加
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo vo){

        try {
            //添加规格表  一张数据  主键返回来
            //规格选项多条数据  外键
            specificationService.add(vo);

            return new Result(true,"提交成功");
        } catch (Exception e) {
          /*  e.printStackTrace();*//**/
            return new Result(false,"提交失败");
        }
    } //添加
    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo vo){

        try {

            //修改  规格表  规格选项表多
            specificationService.update(vo);

            return new Result(true,"提交成功");
        } catch (Exception e) {
          /*  e.printStackTrace();*//**/
            return new Result(false,"提交失败");
        }
    }

    //查询所有品牌
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();
    }

}
