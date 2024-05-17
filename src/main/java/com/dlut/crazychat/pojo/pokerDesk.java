package com.dlut.crazychat.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
牌桌信息
 */
@Data
public class pokerDesk {
    private List<String> boardCards;  //场面牌
    private List<String> hadCards;  //手牌
    private boolean started;  //游戏是否启动
    private int pot;   //当前奖池
    private int money;  //自己的筹码
    private int currentUser_id;   //当前操作的玩家序号
    private int own_id;   //玩家的序号
    private List<texasPlayer> users=new ArrayList<>();  //所有用户的信息
}
