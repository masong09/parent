package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojogroup.GoodsVo;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {


    @Reference
    private GoodsService goodsService;


    //商品列表查询 分页对象 条件
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows,@RequestBody Goods goods){
        return goodsService.search(page,rows,goods);
    }
    //查询一个对象
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){

        return goodsService.findOne(id);
    }
    //开始审核
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){

        try {

            goodsService.updateStatus(ids,status);


            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"失败");
        }
    }
    //删除(批量)
    @RequestMapping("/delete")
    public Result delete(Long[] ids){

        try {

            goodsService.delete(ids);


            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"失败");
        }
    }
}
