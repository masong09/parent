package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 配置自定义的认证类
 */
public class UserDetailServiceImpl implements UserDetailsService{

    //加载用户对象 通过用户名
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询Mysql  商家对象

        Seller seller = sellerService.findSellerByName(username);
        //判断审核是否通过
        if(null != seller && "1".equals(seller.getStatus())){
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            //加密后的密文 不可逆
            return new User(username,seller.getPassword(),authorities);
        }
        return null;
    }
}
