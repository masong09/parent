package cn.itcast.core.pojogroup;

import cn.itcast.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author huyy
 * @Title: Cart
 * @ProjectName parent
 * @Description: 一个商家的购物车  整体的购物车: List<Cart>
 * @date 2018/10/1611:24
 */
public class Cart implements Serializable{

    //商家名称
    private String sellerName;

    //商家id
    private String selleId;

    //购物明细列表
    private List<OrderItem> orderItemList;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSelleId() {
        return selleId;
    }

    public void setSelleId(String selleId) {
        this.selleId = selleId;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
