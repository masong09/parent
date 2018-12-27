package cn.itcast.core.controller;

import cn.itcast.common.utils.CookieUtil;
import cn.itcast.core.pojogroup.Cart;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author huyy
 * @Title: CartController
 * @ProjectName parent
 * @Description: 购物车controller
 * @date 2018/10/1610:53
 */
@RestController
@RequestMapping("/cart")
public class CartController {


    @Reference
    private CartService cartService;

    /**
     * 查询购物车列表
     * @param request
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){

        //判断是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("当前登录人: " + username);

        String cookieValue = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if(StringUtils.isEmpty(cookieValue)){
            cookieValue = "[]";
        }
        List<Cart> cartList = JSON.parseArray(cookieValue, Cart.class);
        if("anonymousUser".equals(username)){
            //未登录,查询cookie购物车列表
            //离线购物车
            return cartList;
        }else{
            //购物车合并
            //已登录,查询redis购物车列表
            List<Cart> redis_cartList = cartService.findCartListFromRedis(username);
            if(cartList.size() > 0){//判断cookie中是否有购物车
                //有才进行合并操作
                redis_cartList = cartService.mergeCartList(cartList,redis_cartList);

                //合并后的结果重新放到redis中
                cartService.saveCartListToRedis(redis_cartList,username);

                //清空cookie购物车
                CookieUtil.deleteCookie(request,response,"cartList");
            }

         return redis_cartList;
        }


    }



    /**
     * 添加商品到购物车
     * @param itemId : 商品skuid
     * @param num : 数量
     * @return : 是否成功
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9003")
    public Result addGoodsToCartList(Long itemId, Integer num,HttpServletRequest request, HttpServletResponse response){

       /* response.setHeader("Access-Control-Allow-Origin","http://localhost:9003");
        response.setHeader("Access-Control-Allow-Credentials","true");*/

        try {

            //判断是否登录
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录人: " + username);
            List<Cart> cartList = findCartList(request,response);
            cartList = cartService.addGoodsToCartList(itemId,num,cartList);
            if("anonymousUser".equals(username)){
                //未登录,查询cookie购物车列表
                //1. 取出来cookie中购物车列表 cartList

                //2.itemId  num   cartList
                //cartList = 调用cartService.addGoodsToCartList(itemId,num,cartList);

                String cartString = JSON.toJSONString(cartList);
                //3. 重新写回到客户端的cookie浏览器里面去
                CookieUtil.setCookie(request,response,"cartList",cartString,3600*24,"utf-8");

            }else{
                //已登录,添加商品到redis购物车列表
                //1. 查询redis购物车列表
                //2. 调用service,完成添加商品到购物车列表

                //3. 将我们的redis购物车重新写会到redis中
                cartService.saveCartListToRedis(cartList,username);
            }
            return new Result(true,"添加购物车成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }
    }
}
