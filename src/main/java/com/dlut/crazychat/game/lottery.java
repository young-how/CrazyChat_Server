package com.dlut.crazychat.game;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Data
public class lottery {
    private int bonus;  //当前奖池奖金

    @Value("${game.lottery.base_max}")
    private int base_max;
    @Value("${game.lottery.base}")
    private int base;  //基础奖池奖金
    @Value("${game.lottery.one_ticked}")
    private int prize;  //单张彩票价格
    @Value("${game.lottery.max_ticked}")
    private int max_ticked;  //单次买的最大彩票数
    @Value("${game.lottery.add_per_note}")
    private int add;   //每张彩票增加的奖池奖金
    @Value("${game.lottery.probability_1}")
    private double probability_1;  //一等奖概率
    @Value("${game.lottery.probability_2}")
    private double probability_2;  //2等奖概率
    @Value("${game.lottery.probability_3}")
    private double probability_3;  //3等奖概率
    @Value("${game.lottery.probability_4}")
    private double probability_4;  //4等奖概率
    @Value("${game.lottery.probability_5}")
    private double probability_5;  //5等奖概率
    @Value("${game.lottery.probability_6}")
    private double probability_6;  //6等奖概率
    private Random rand=new Random();
    @PostConstruct
    public void init(){
        resetTarget();  //重置target数
    }
    public void resetTarget(){
        bonus=base;
    }
    public void add_bonus(){
        //奖池拓张
        bonus+=add;
        bonus=Math.min(bonus,base_max);
    }
    public String explain(){
        StringBuilder re=new StringBuilder();
        re.append("彩票游戏规则:\n");
        re.append("发送#lt 买一张彩票，#lt n 买n张彩票\n");
        re.append("每张彩票消费:"+prize+"  每买一张彩票会增加奖池:"+add+"\n");
        re.append("1等奖:获取奖池所有积分，概率："+probability_1+"\n");
        re.append("2等奖:获取奖池十分之一的积分，概率："+probability_2+"\n");
        re.append("3等奖:获取奖池百分之一的积分，概率："+probability_3+"\n");
        re.append("4等奖:获取奖池千分之一的积分，概率："+probability_4+"\n");
        re.append("5等奖:获取奖池万分之一的积分，概率："+probability_5+"\n");
        re.append("6等奖:获取奖池十万分之一的积分，概率："+probability_6+"\n");
        return re.toString();
    }
    public Map<String,Integer> buy(int num){
        //买的彩票数，返回的Integer是获利的钱
        long seed = System.currentTimeMillis(); // 获取当前时间戳作为种子
        rand=new SecureRandom(); //重置随机器
        Map<String,Integer> re=new HashMap<>();
        StringBuilder result=new StringBuilder("中奖结果:");
        num=Math.min(num,max_ticked);
        int sum=-num*prize;  //初始盈利值
        for(int i=0;i<num;i++){
            double rate=rand.nextDouble();  //当前生成的概率
            if(rate<probability_1){
                result.append("一等奖:"+bonus+"积分， ");
                sum+=bonus;
                resetTarget();
                re.put("price1",re.getOrDefault("price1",0)+1);
            }
            else if(rate<probability_2){
                int reward=(int)(bonus*0.06);
                result.append("二等奖:"+reward+"积分， ");
                sum+=reward;
                bonus-=reward;
                //re.put("price2",re.getOrDefault("price2",0)+1);
            }
            else if(rate<probability_3){
                int reward=(int)(bonus*0.004);
                result.append("三等奖:"+reward+"积分， ");
                sum+=reward;
                bonus-=reward;
                //re.put("price3",re.getOrDefault("price3",0)+1);
            }
            else if(rate<probability_4){
                int reward=(int)(bonus*0.0008);
                result.append("四等奖:"+reward+"积分， ");
                sum+=reward;
                bonus-=reward;
                //re.put("price4",re.getOrDefault("price4",0)+1);
            }
            else if(rate<probability_5){
                int reward=(int)(bonus*0.00024);
                result.append("五等奖:"+reward+"积分， ");
                sum+=reward;
                bonus-=reward;
                //re.put("price5",re.getOrDefault("price5",0)+1);
            }
            else if(rate<probability_6){
                int reward=(int)(bonus*0.00003);
                result.append("六等奖:"+reward+"积分， ");
                sum+=reward;
                bonus-=reward;
                //re.put("price6",re.getOrDefault("price6",0)+1);
            }
            else{
                result.append("未中奖! ");
            }
            add_bonus();  //奖池增加
        }
        re.put(result.toString(),sum);
        return re;
    }

    public static void main(String[] args) {
        lottery lt=new lottery();
        lt.setBase(600000);
        lt.setBonus(600000);
        lt.setBase_max(2000000);
        lt.setAdd(50);
        lt.setPrize(30);
        lt.setMax_ticked(100000);
        lt.setProbability_1(0.00001);
        lt.setProbability_2(0.00003);
        lt.setProbability_3(0.001);
        lt.setProbability_4(0.01);
        lt.setProbability_5(0.1);
        lt.setProbability_6(0.3);
        int re_max=0;
        int re_min=0;
        int sum=0;
        int num_price1=0;
        int num_price2=0;
        int num_price3=0;
        int num_price4=0;
        int num_price5=0;
        int num_price6=0;
        for(int i=0;i<100;i++){
            Map<String,Integer> map=lt.buy(10);
            int re=0;
            for(String s: map.keySet()){
                re=map.get(s);
            }
            sum+=re;
            re_max=Math.max(re_max,re);
            re_min=Math.min(re_min,re);
//            num_price1+=map.getOrDefault("price1",0);
//            num_price2+=map.getOrDefault("price2",0);
//            num_price3+=map.getOrDefault("price3",0);
//            num_price4+=map.getOrDefault("price4",0);
//            num_price5+=map.getOrDefault("price5",0);
//            num_price6+=map.getOrDefault("price6",0);


        }
//        System.out.println(String.format("%d, %d, %d, %d, %d, %d",
//                num_price1,num_price2,num_price3,num_price4,num_price5,num_price6));
        System.out.println("最大值:"+re_max);
        System.out.println("最小值:"+re_min);
        System.out.println("总和:"+sum);
    }
}
