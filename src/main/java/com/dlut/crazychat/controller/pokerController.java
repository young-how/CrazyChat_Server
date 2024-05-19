package com.dlut.crazychat.controller;

import com.dlut.crazychat.pojo.pokerDesk;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.texaspokerService;
import com.dlut.crazychat.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static java.lang.Thread.sleep;

/*
德州扑克的控制器
 */
@Controller
public class pokerController {
    @Autowired
    private texaspokerService pokerservice;
    @Autowired
    private userService userservice;

    @PostMapping("/texasPoker/getDeskInfo")
    public ResponseEntity<pokerDesk> getDeskInfo(@RequestBody userStat user){
        //根据用户信息返回牌局状态
        if(user==null) return ResponseEntity.notFound().build(); //未找到该用户
        try{
            pokerDesk re=pokerservice.getDeskInfo(user);  //返回牌局信息
            return ResponseEntity.ok().body(re);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/texasPoker/join")
    public ResponseEntity<pokerDesk> joinGame(@RequestBody userStat user, @RequestParam("money") int money){
        //根据用户信息返回牌局状态
        int user_money=userservice.findUserByID(user.getId()).getScore();  //用户当前账户积分
        if(user==null||user_money<=0) return ResponseEntity.notFound().build(); //积分不足或账户为空
        try{
            money=Math.min(user_money,money);  //积分的最小值
            if(pokerservice.joinRoom(user,money)){
                //加入游戏成功
                userservice.addScore(user,-money);  //扣除相应的积分
            }
            pokerDesk re=pokerservice.getDeskInfo(user);  //返回牌局信息
            return ResponseEntity.ok().body(re);
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }
    @PostMapping("/texasPoker/exit")
    public ResponseEntity<pokerDesk> exitGame(@RequestBody userStat user){
        //根据用户信息返回牌局状态
        if(user==null) return ResponseEntity.notFound().build(); //积分不足或账户为空
        try {
            int reward=pokerservice.exitRoom(user);
            userservice.addScore(user,reward);  //扣除相应的积分
            pokerDesk re=pokerservice.getDeskInfo(user);  //返回牌局信息
            return ResponseEntity.ok().body(re);
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }
    @PostMapping("/texasPoker/start")
    public ResponseEntity<pokerDesk> startGame(@RequestBody userStat user){
        //根据用户信息返回牌局状态
        if(user==null) return ResponseEntity.notFound().build(); //积分不足或账户为空
        try{
            pokerservice.startGame();
            pokerDesk re=pokerservice.getDeskInfo(user);  //返回牌局信息
            return ResponseEntity.ok().body(re);
        }
        catch (RuntimeException | CloneNotSupportedException e){
            return  ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/texasPoker/bet")
    public ResponseEntity<pokerDesk> bet(@RequestBody userStat user,@RequestParam("money") int money,@RequestParam("fold") boolean fold){
        //根据用户信息返回牌局状态
        if(user==null) return ResponseEntity.notFound().build(); //积分不足或账户为空
        try{
            boolean isCurrent=pokerservice.isCurrentUser(user);
            boolean betSuccess=false;
            if(isCurrent){
                //为当前操作用户
                if(fold){
                    pokerservice.fold();//弃牌
                }else{
                    betSuccess=pokerservice.bet(money);  //下注
                }
            }
            sleep(50);
            pokerDesk re=pokerservice.getDeskInfo(user);  //返回牌局信息
            if(!isCurrent) re.setSystemInfo("当前不是你的回合，操作失败!\n");
            else if (!betSuccess) {
                re.setSystemInfo("下注金额不够!\n");
            } else re.setSystemInfo("操作成功!\n");
            return ResponseEntity.ok().body(re);
        }
        catch (RuntimeException | CloneNotSupportedException e){
            return  ResponseEntity.notFound().build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
