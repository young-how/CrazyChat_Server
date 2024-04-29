package com.dlut.crazychat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class userStat {
    private String id;  //标识用户，mac地址
    private String name;  //用户名
    private int message_num;  //发言数目
    private int score;  //积分
    private int level;  //等级
    private String title;  //称号
    private int win_game_num;
    private int game_num;
    private List<String> reward;
    private  boolean isActive;  //是否是活跃状态
    private  Long rank;  //积分排名
    private  Long activeNum;  //活跃人数
}