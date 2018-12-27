package cn.itcast.core.controller;


import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.PatternSyntaxException;

/**
 * @author huyy
 * @Title: UserController
 * @ProjectName parent
 * @Description: TODO
 * @date 2018/10/159:52
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;


    @RequestMapping("/add")
    public Result add(@RequestBody  User user, String smscode){

        try {
            //1.校验验证码是否输入一致: 用户输入的验证码和之前发送的验证码进行比对
          /*  boolean checkCodeLegal = userService.checkSmsCode(user.getPhone(),smscode);

            if(!checkCodeLegal){
                return  new Result(false,"验证码输入有误!");
            }*/
            //2.如果校验成功,进行用户注册
            userService.add(user);
            return new Result(true,"注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }


}
