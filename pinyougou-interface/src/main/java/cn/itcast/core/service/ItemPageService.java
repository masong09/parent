package cn.itcast.core.service;

/**
 * @author huyy
 * @Title: ItemPageService
 * @ProjectName parent
 * @Description: 商品详情页面的服务接口
 * @date 2018/10/129:13
 */
public interface ItemPageService {

    /**
     * 根据spu的id生成静态页面
     * @param goodsId
     */
    public void genItemPage(Long goodsId);
}
