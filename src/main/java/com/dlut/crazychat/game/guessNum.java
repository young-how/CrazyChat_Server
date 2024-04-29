package com.dlut.crazychat.game;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Data
public class guessNum {
    private int target;
    private int bonus;  //累积奖金
    @Value("${game.guess.money_per_time}")
    private int money_per_time;  //单次猜的消费
    @Value("${game.guess.basebonus}")
    private int base_bonus;   //基础奖金
    @Value("${game.guess.bound}")
    private int bound;   //猜词范围
    private Random rand=new Random();
    @PostConstruct
    public void init(){
        resetTarget();  //重置target数
    }
    public void resetTarget(){
        target=rand.nextInt(bound)+1;
        bonus=base_bonus;  //重置累积奖励
    }
    public String explain(){
        StringBuilder re=new StringBuilder();
        re.append("猜词游戏规则:\n");
        re.append("系统会随机生成一个数字(0-1000)，发送#gs n 去猜这个隐藏的数字\n");
        re.append("每次猜词会消耗:"+money_per_time+"\n");
        re.append("消耗的积分会累积到奖池中，奖池初始积分200分，猜中的玩家会获得奖池中所有的积分。\n");
        return re.toString();
    }
    public int guess(int num){
        //rand=new Random();
        if(num==target){
            int reward=bonus;
            resetTarget();
            return reward;
        }
        else if(num>target){
            bonus+=money_per_time;
            return 1;   //猜大了
        }
        else if(num<target){
            bonus+=money_per_time;
            return -1; //猜小了
        }
        return 0;
    }
}
