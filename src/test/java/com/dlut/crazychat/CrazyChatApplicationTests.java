package com.dlut.crazychat;

import com.dlut.crazychat.game.dailySign;
import com.dlut.crazychat.game.findSpy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class CrazyChatApplicationTests {
    @Autowired
    private findSpy sp;
    @Test
    void contextLoads() {
        System.out.println(sp.getKeys().size());
//        System.out.println(sp.getWord());
        sp.init();
    }

}
