package com.dlut.crazychat;

import com.dlut.crazychat.game.dailySign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrazyChatApplicationTests {
    @Autowired
    public dailySign sign;
    @Test
    void contextLoads() {
        int re=0;
        for(int i=1;i<30;i++){
            int reward=sign.calReward(i,i);
            re+=reward;
            System.out.println(reward);
        }
        System.out.println("签到总奖励:"+re);
    }

}
