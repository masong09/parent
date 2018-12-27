package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

/**
 * @author huyy
 * @Title: UserService
 * @ProjectName parent
 * @Description: 和用户相关的服务接口
 * @date 2018/10/159:45
 */
public interface UserService {


    /**
     * 用户注册
     * @param user
     */
    public void add(User user);


}
