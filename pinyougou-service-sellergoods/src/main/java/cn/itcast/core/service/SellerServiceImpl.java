package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 商家管理
 */
@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;
    //添加商家
    public void add(Seller seller){


        BCryptPasswordEncoder  encode =  new BCryptPasswordEncoder();
        seller.setPassword(encode.encode(seller.getPassword()));
        //状态  0: 未审核
        seller.setStatus("0");
        //时间
        seller.setCreateTime(new Date());
        sellerDao.insertSelective(seller);
    }

    //根据用户名查询商家对象
    public Seller findSellerByName(String sellerId){
        return sellerDao.selectByPrimaryKey(sellerId);
    }

}
