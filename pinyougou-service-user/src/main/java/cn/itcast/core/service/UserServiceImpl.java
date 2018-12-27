package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huyy
 * @Title: UserServiceImpl
 * @ProjectName parent
 * @Description: 用户服务实现者
 * @date 2018/10/159:47
 */
@Service
public class UserServiceImpl implements  UserService {

    @Autowired
    private UserDao userDao;
    /**
     * 用户注册
     *
     * @param user
     */
    @Override
    public void add(User user) {
        //进行用户注册
        //1.对密码加密,再放进去
       /* String md5Hex = DigestUtils.md5Hex(user.getPassword());
        user.setPassword(md5Hex);
*/
        //2. 设置时间
        user.setCreated(new Date());
        user.setUpdated(new Date());
        userDao.insert(user);
    }


}
