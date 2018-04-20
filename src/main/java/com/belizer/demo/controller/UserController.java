package com.belizer.demo.controller;

import com.belizer.demo.service.UserService;
import com.belizer.spring.annotation.Autowired;
import com.belizer.spring.annotation.Controller;
import com.belizer.spring.annotation.RequestMapping;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    public void getList(){
        userService.getList();
    }
}
