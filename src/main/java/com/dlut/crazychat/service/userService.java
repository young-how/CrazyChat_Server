package com.dlut.crazychat.service;

import com.dlut.crazychat.utils.SystemManager;
import com.dlut.crazychat.pojo.rankList;
import com.dlut.crazychat.pojo.serviceStat;
import com.dlut.crazychat.pojo.userStat;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Aspect
@Service
public class userService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String rankListKey="rankList";
    private final String activeListKey="activeList";
    private List<userStat> activeUser=new ArrayList<>();  //活跃用户的列表
    private serviceStat serviceStat;  //服务的状态
//    @Autowired
//    private SystemManager Manager;  //系统消息发送器

    @Autowired
    public userService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @PostConstruct
    public void init(){
        //Manager.start();  //启动命令监听线程
    }
    public void setValue(String key, Object value) {
        // 使用RedisTemplate的opsForValue()方法获取ValueOperations接口实例，然后调用set()方法存储键值对
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getValue(String key) {
        // 使用RedisTemplate的opsForValue()方法获取ValueOperations接口实例，然后调用get()方法根据键获取值
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteKey(String key) {
        // 调用RedisTemplate的delete()方法根据键删除对应的键值对
        redisTemplate.delete(key);
    }
    public userStat findUserByID(String id){
        return (userStat) getValue(id);  //根据id返回用户
    }

    public userStat createNewUser(userStat userInput){
        //根据userInput信息创建新用户
        userStat user=new userStat();
        user.setId(userInput.getId());
        user.setName(userInput.getName());
        user.setLevel(1);   //初始等级
        user.setScore(1000); //初始分数
        user.setActive(true);  //活跃与否
        setValue(user.getId(),user);  //添加用户
        return user;
    }
    public userStat find_Update_User(userStat user){
        //查找redis中的用户信息并更新用户状态
        if(getValue(user.getId())!=null){
            userStat userInRedis=(userStat) getValue(user.getId());
            userInRedis.setName(user.getName());
            setValue(user.getId(),userInRedis);  //每查一次会重新设置一下该对象
            return userInRedis;  //存在用户，更新状态进redis并返回
        }
        //user=updateRankList(user);  //更新排名
        return createNewUser(user); //根据信息创建user到redis
    }

    public userStat addScore(userStat user,int increasement){
        user=find_Update_User(user);  //先去查找是否用户注册过
        user.setScore(user.getScore()+increasement);  //变更积分
        user.setLevel(user.getScore()/1000);  //计算等级
        setValue(user.getId(),user);  //在redis中缓存user
        user=updateRankList(user);  //更新排名
        return user;
    }

    public userStat addMessage(userStat user){
        user=find_Update_User(user);  //先去查找是否用户注册过
        user.setMessage_num(user.getMessage_num()+1);  //变更发言数目
        user.setScore(user.getScore()+10);  //变更积分
        user.setLevel(user.getScore()/1000);  //计算等级
        setValue(user.getId(),user);  //在redis中缓存user
        user=updateRankList(user);  //更新排名
        return user;
    }

    public userStat updateRankList(userStat user){
        String member=user.getId();
        Double score=(double)user.getScore();
        //对排行榜信息进行更新
        Double currentScore=redisTemplate.opsForZSet().score(rankListKey,member);  //设置最新的分数
        if (currentScore != null) {
            // 如果成员存在，则更新其分数
            redisTemplate.opsForZSet().add(rankListKey, member, score);
        } else {
            // 如果成员不存在，则将其添加到有序集合中
            redisTemplate.opsForZSet().add(rankListKey, member, score);
        }
        Long rank=redisTemplate.opsForZSet().reverseRank(rankListKey,member)+1;
        user.setRank(rank);  //设置排名
        setValue(user.getId(),user);  //在redis中缓存user
        user=updateActiveList(user);  //更新活跃度
        return user;
    }
    public userStat updateActiveList(userStat user){
        //添加活跃列表，先更新排行榜，再更新活跃列表
        String member=user.getId();
        Double score=(double)System.currentTimeMillis();
        //对排行榜信息进行更新
        Double currentScore=redisTemplate.opsForZSet().score(activeListKey,member);  //设置最新的分数
        if (currentScore != null) {
            // 如果成员存在，则更新其分数
            redisTemplate.opsForZSet().add(activeListKey, member, score);
        } else {
            // 如果成员不存在，则将其添加到有序集合中
            redisTemplate.opsForZSet().add(activeListKey, member, score);
        }
        //统计近半个小时的用户数
        double currentTimestamp=System.currentTimeMillis();  //当前时间戳
        double halfhour_befor=currentTimestamp - (30 * 60 * 1000);  //半小时前的时间戳
        Long activeNum=redisTemplate.opsForZSet().count(activeListKey,halfhour_befor,currentTimestamp);
        //serviceStat.setActiveNum(activeNum);
        user.setActiveNum(activeNum);
        setValue(user.getId(),user);  //在redis中缓存user
        return user;
    }
    public rankList getRankList(){
        rankList rank=new rankList();
        //返回排行榜
        double minScore = Double.NEGATIVE_INFINITY; // 最小分数
        double maxScore = Double.POSITIVE_INFINITY; // 最大分数
        // 设置为从大到小排序
        boolean reversed = true;
        Set<Object> members = redisTemplate.opsForZSet().rangeByScore(rankListKey, minScore, maxScore);
        for (Object member : members) {
            String id=(String) member;
            userStat user=(userStat) getValue(id);
            rank.addUsrRank(user);  //按顺序加入列表
        }
        Collections.reverse(rank.getUsers());
        return rank;
    }
}
