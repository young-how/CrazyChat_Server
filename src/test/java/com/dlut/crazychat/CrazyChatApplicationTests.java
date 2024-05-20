package com.dlut.crazychat;

import com.dlut.crazychat.game.Ollama_robot;
import com.dlut.crazychat.game.dailySign;
import com.dlut.crazychat.game.findSpy;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.texaspokerService;
import com.dlut.crazychat.utils.PokerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        //robot.askOllama("天空为什么是蓝色的");
        //robot.askOllamaByStream("帮我写一段java用来生成uuid的代码");
    }
    @Autowired
    private texaspokerService pokerService;

    @Test
    public void testDealCards() {
        int playerCount = 6;
        Map<String, Object> result = pokerService.dealCards(playerCount);

        List<List<String>> playersHands = (List<List<String>>) result.get("playersHands");
        List<String> boardCards = (List<String>) result.get("boardCards");
        List<String> winer=null;
        for(List<String> cards:playersHands){
            if(winer==null){
                winer=cards;
            }
            else{
                PokerUtils.Hand gamer=PokerUtils.evaluateBestHand(cards,boardCards);  //评估场面牌+手牌
                PokerUtils.Hand winer_hand=PokerUtils.evaluateBestHand(winer,boardCards);  //评估场面牌+手牌
                if(gamer.compareTo(winer_hand)==1){
                    System.out.println(boardCards+"场面牌   "+cards+"牌型:"+gamer.rank+"大于："+winer+" 牌型："+winer_hand.rank);
                    winer=cards;
                }
                else if(gamer.compareTo(winer_hand)==-1){
                    System.out.println(boardCards+"场面牌   "+cards+"牌型:"+gamer.rank+"小于："+winer+" 牌型："+winer_hand.rank);
                }
                else{
                    System.out.println(boardCards+"场面牌   "+cards+"牌型:"+gamer.rank+"等于："+winer+" 牌型："+winer_hand.rank);
                }
            }
        }
    }
    @Test
    public void testRunOneTurn(){
//        userStat user1=new userStat();
//        userStat user2=new userStat();
//        userStat user3=new userStat();
//        userStat user4=new userStat();
//        user1.setId("user1");
//        user2.setId("user2");
//        user3.setId("user3");
//        user4.setId("user4");
//        pokerService.joinRoom(user1,3000);
//        pokerService.joinRoom(user2,2000);
//        pokerService.joinRoom(user3,1000);
//        pokerService.joinRoom(user4,500);
//        pokerService.nextBettingRound();
    }
    @Test
    public void wholeGame(){
//        userStat user1=new userStat();
//        userStat user2=new userStat();
//        userStat user3=new userStat();
//        userStat user4=new userStat();
//        userStat user5=new userStat();
//        userStat user6=new userStat();
//        user1.setId("user1");
//        user2.setId("user2");
//        user3.setId("user3");
//        user4.setId("user4");
//        user5.setId("user5");
//        user6.setId("user6");
//        pokerService.joinRoom(user1,3000);
//        pokerService.joinRoom(user2,2000);
//        pokerService.joinRoom(user3,1000);
//        pokerService.joinRoom(user4,500);
//        pokerService.joinRoom(user5,60000);
//        pokerService.joinRoom(user6,8000);
//        for(int t=0;t<10;t++){
//            pokerService.startGame();  //开始游戏
//            for(int i=0;i<3;i++){
//                //下三轮注
//                pokerService.setHighestBet(100*(i+1));
//                pokerService.nextBettingRound();
//            }
//        }
    }
}
