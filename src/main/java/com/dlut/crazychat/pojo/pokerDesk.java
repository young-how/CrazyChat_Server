package com.dlut.crazychat.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
牌桌信息
 */
@Data
public class pokerDesk {
    private List<String> boardCards=new ArrayList<>();  //场面牌
    private List<String> hadCards=new ArrayList<>();  //手牌
    private boolean started;  //游戏是否启动
    private int pot;   //当前奖池
    private int money;  //自己的筹码
    private int currentUser_id;   //当前操作的玩家序号
    private int own_id=-1;   //玩家的序号
    private List<texasPlayer> users=new ArrayList<>();  //所有用户的信息
    private int round;  //当前的下注轮次
    private int currentHighestBet;  //当前最高下注金额
    private String systemInfo;  //系统消息
    private String winner;  //赢家信息
    private List<String> winner_cards=new ArrayList<>();  //赢家手牌
}
