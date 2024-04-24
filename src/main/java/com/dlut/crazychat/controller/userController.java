package com.dlut.crazychat.controller;

import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class userController {
    @Autowired
    public userService userservice;
    //前端获取用户状态
    @PostMapping("/getState")
    public ResponseEntity<userStat> getUserStat(@RequestBody userStat userstat){
        //根据前端返回的json对象查找redis，并更新redis状态
        return ResponseEntity.ok().body(userservice.find_Update_User(userstat));
    }
    //前端发送消息，进行加分以及添加发言数目
    @PostMapping("/sendMessage")
    public ResponseEntity<userStat> sendMessage(@RequestBody userStat userstat){
        //根据前端返回的json对象查找redis，并更新redis状态
        return ResponseEntity.ok().body(userservice.addMessage(userstat));
    }

}
