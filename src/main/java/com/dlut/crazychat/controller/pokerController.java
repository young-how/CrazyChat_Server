package com.dlut.crazychat.controller;

import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.texaspokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
德州扑克的控制器
 */
@Controller
public class pokerController {
    @Autowired
    private texaspokerService pokerservice;

    @GetMapping("/texasPoker/getDeskInfo")
    public String getDeskInfo(@RequestParam("user") userStat user){
        //根据用户信息返回牌局状态
        return "test";
    }
}
