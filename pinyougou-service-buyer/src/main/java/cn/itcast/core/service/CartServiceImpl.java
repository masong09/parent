package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huyy
 * @Title: CartServiceImpl
 * @ProjectName parent
 * @Description: TODO
 * @date 2018/10/1612:11
 */
@Service
public class CartServiceImpl implements CartService {

    private Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private ItemDao itemDao;
    /**
     * 添加商品到一个购物车列表
     *
     * @param itemId   : 商品skuid
     * @param num      : 数量
     * @param cartList : 已经存在的购物车列表
     * @return : 添加后的购物车列表
     */
    @Override
    public List<Cart> addGoodsToCartList(Long itemId, Integer num, List<Cart> cartList) {
        //1.根据商品SKU ID查询SKU商品信息
        Item item = itemDao.selectByPrimaryKey(itemId);

        //2.获取商家ID
        String sellerId = item.getSellerId();
        String sellerName = item.getSeller();


        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCart(sellerId,cartList);
        if(cart == null){
            //4.如果购物车列表中不存在该商家的购物车
            //4.1新建购物车对象
            cart = new Cart();
            cart.setSelleId(sellerId);
            cart.setSellerName(sellerName);
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);


        }else{
            //5.如果购物车列表中存在该商家的购物车
            OrderItem orderItem = searchOrderItem(itemId,cart.getOrderItemList());
            if(orderItem == null){
                // 查询购物车明细列表中是否存在该商品

                //5.1. 如果没有，新增购物车明细
                orderItem = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);

            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改小计
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));


                //TODO
                //商品数量等于0,移除该商品
                if(orderItem.getNum() <= 0){
                    cart.getOrderItemList().remove(orderItem);
                }

                //当前购物车中不存在购物明细,移除该购物车
                if(cart.getOrderItemList().size() == 0){
                    cartList.remove(cart);

                }

            }


        }

        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询redis中的购物车列表
     *
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        Object object = redisTemplate.boundHashOps("cartList").get(username);
        if(object == null){
            return new ArrayList<>();
        }
        return (List<Cart>)(object);

    }

    /**
     * 保存购物车列表到redis中
     *
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(List<Cart> cartList,String username) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并购物车
     *
     * @param cartList       : cookie购物车
     * @param redis_cartList : redis购物车
     * @return 合并购物车
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList, List<Cart> redis_cartList) {

        for (Cart cart : cartList) {
            for (OrderItem orderItem : cart.getOrderItemList()) {
               redis_cartList =  addGoodsToCartList(orderItem.getItemId(),orderItem.getNum(),redis_cartList);
            }
        }
        return redis_cartList;
    }

    /**
     * 判断当前购物车中是否存在该要添加的商品
     * @param itemId : 商品的skuid
     * @param orderItemList : 购物明细列表
     * @return 查找后的购物明细
     */
    private OrderItem searchOrderItem(Long itemId, List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据item对象创建orderItem对象
     * @param item : sku对象
     * @return
     */
    private OrderItem createOrderItem(Item item,Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPrice(item.getPrice());
        orderItem.setPicPath(item.getImage());
        orderItem.setTitle(item.getTitle());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));//小计
        return orderItem;
    }

    /**
     * 根据商家ID判断购物车列表中是否存在该商家的购物车
     * @param sellerId : 商家的id
     * @param cartList : 购物车列表
     * @return 购物车
     */
    private Cart searchCart(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if(cart.getSelleId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
