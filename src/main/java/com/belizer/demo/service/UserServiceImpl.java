package com.belizer.demo.service;

import com.belizer.spring.annotation.Service;

@Service("userService")
public class UserServiceImpl implements UserService {
    public void getList(){
        System.out.println("获取到用户列表：小米，小红，小刚，小李");
    }
}
