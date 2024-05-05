package com.dlut.crazychat;

import com.dlut.crazychat.game.dailySign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class CrazyChatApplicationTests {

    @Test
    void contextLoads() {
        HashMap<String,String> mp=new HashMap<>();
        mp.put("baidu","baidu");
        mp.put("tencent","baidu");
        mp.put("huawei","baidu");
        mp.put("ali","baidu");
        for(String key:mp.keySet()){
            System.out.println(key);
        }
    }

}
