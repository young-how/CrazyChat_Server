package com.dlut.crazychat;

import com.dlut.crazychat.game.Ollama_robot;
import com.dlut.crazychat.game.dailySign;
import com.dlut.crazychat.game.findSpy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

@SpringBootTest
class CrazyChatApplicationTests {
    @Autowired
    private findSpy sp;
    @Autowired
    private Ollama_robot robot;
    @Test
    void contextLoads() {
        String str = "#give 04-7C-16-0B-67-68 1000 \n";

        // 匹配 MAC 地址的正则表达式
        String macRegex = "(?i)([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})";


        // 编译正则表达式
        Pattern macPattern = Pattern.compile(macRegex);

        String macAddress="";
        // 匹配 MAC 地址
        Matcher macMatcher = macPattern.matcher(str);
        if (macMatcher.find()) {
             macAddress = macMatcher.group();
            System.out.println("MAC 地址: " + macAddress);
        } else {
            System.out.println("未找到 MAC 地址。");
        }

        // 匹配数字
        // 匹配数字的正则表达式
        String numberRegex = macAddress+" (\\d+)";
        Pattern numberPattern = Pattern.compile(numberRegex);
        Matcher numberMatcher = numberPattern.matcher(str);
        if (numberMatcher.find()) {
            String number = numberMatcher.group(1);
            System.out.println("数字: " + number);
        } else {
            System.out.println("未找到数字。");
        }
    }
    @Test
    public void testLottery(){
        //测试彩票
    }
    @Test
    public void testRobot(){
        robot.askOllama("用中文！请介绍一下你的功能");

    }

}
