package cn.itcast.core.service;

import cn.itcast.core.pojogroup.Cart;

import java.util.List;

/**
 * @author huyy
 * @Title: CartService
 * @ProjectName parent
 * @Description: 购物车服务接口
 * @date 2018/10/1612:10
 */
public interface CartService {


    /**
     * 添加商品到一个购物车列表
     * @param itemId : 商品skuid
     * @param num : 数量
     * @param cartList : 已经存在的购物车列表
     * @return : 添加后的购物车列表
     */
    List<Cart> addGoodsToCartList(Long itemId, Integer num, List<Cart> cartList);

    /**
     * 查询redis中的购物车列表
     * @return
     */
    List<Cart> findCartListFromRedis(String username);

    /**
     * 保存购物车列表到redis中
     * @param cartList
     */
    void saveCartListToRedis(List<Cart> cartList, String username);

    /**
     * 合并购物车
     * @param cartList : cookie购物车
     * @param redis_cartList : redis购物车
     * @return 合并购物车
     */
    List<Cart> mergeCartList(List<Cart> cartList, List<Cart> redis_cartList);
}
