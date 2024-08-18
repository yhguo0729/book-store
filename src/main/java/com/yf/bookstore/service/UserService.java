package com.yf.bookstore.service;


import com.yf.bookstore.model.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务接口
 */
public interface UserService extends UserDetailsService {

    /**
     * 注册新用户
     *
     * @param user 待注册的用户信息
     * @return 注册成功的用户对象
     */
    User register(User user);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功后返回的token字符串
     */
    String login(String username, String password);

    /**
     * 查询用户信息
     *
     * @return 返回查询到的用户信息
     */
    User getUserInfo();
}


