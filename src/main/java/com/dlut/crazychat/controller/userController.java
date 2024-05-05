package com.dlut.crazychat.controller;

import com.dlut.crazychat.pojo.rankList;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.userService;
import com.dlut.crazychat.utils.SystemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
public class userController {
    @Autowired
    public userService userservice;
    @Autowired
    private SystemManager Sys;
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
    @PostMapping("/joinFindSpy")
    public ResponseEntity<String> joinFindSpy(@RequestBody userStat userstat){
        //模拟用户加入游戏
        StringBuilder re=new StringBuilder(Sys.getGameservice().getFindspy().join(userstat));
        //re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
        return ResponseEntity.ok().body(re.toString());
    }
    @PostMapping("/userVote")
    public ResponseEntity<String> userVote(@RequestBody userStat userstat,@RequestParam("num") int num){
        //模拟用户加入游戏
        StringBuilder re=new StringBuilder();
        re.append(Sys.getGameservice().getFindspy().vote(userstat,num));
        re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
//        StringBuilder re=new StringBuilder(Sys.getGameservice().getFindspy().join(userstat));
//        re.append("\n你的身份是:"+Sys.getGameservice().getFindspy().getUserRole(userstat));
        return ResponseEntity.ok().body(re.toString());
    }
    @PostMapping("/rankList")
    public ResponseEntity<rankList> getRankList(){
        //查看积分榜
        rankList rank=userservice.getRankList();
        return ResponseEntity.ok().body(rank);
    }
    @GetMapping("/systemMessage")
    @ResponseBody
    public String sendSystemMessage(@RequestParam("id") String id){
        //查看积分榜
        Sys.send("test",id);
        return id;
    }

}
