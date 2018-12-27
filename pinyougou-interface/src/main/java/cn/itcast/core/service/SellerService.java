package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {

    //添加商家
    public void add(Seller seller);

    //根据用户名查询商家对象
    public Seller findSellerByName(String sellerId);
}
