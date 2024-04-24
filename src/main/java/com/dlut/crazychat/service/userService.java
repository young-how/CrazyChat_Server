package com.dlut.crazychat.service;

import com.dlut.crazychat.pojo.SystemMessageSender;
import com.dlut.crazychat.pojo.userStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class userService {

    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SystemMessageSender sender;  //系统消息发送器

    @Autowired
    public userService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
    public void addUser(){

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
        return createNewUser(user); //根据信息创建user到redis
    }

    public userStat addScore(userStat user,int increasement){
        user=find_Update_User(user);  //先去查找是否用户注册过
        user.setScore(user.getScore()+increasement);  //变更积分
        user.setLevel(user.getScore()/1000);  //计算等级
        setValue(user.getId(),user);  //在redis中缓存user
        return user;
    }
    public userStat addMessage(userStat user){
        user=find_Update_User(user);  //先去查找是否用户注册过
        user.setMessage_num(user.getMessage_num()+1);  //变更发言数目
        user.setScore(user.getScore()+10);  //变更积分
        user.setLevel(user.getScore()/1000);  //计算等级
        setValue(user.getId(),user);  //在redis中缓存user
        return user;
    }
    //执行事件后状态发生变化，将发送系统消息
//    public userStat statusChange(userStat user){
//        user=find_Update_User(user);  //先去查找是否用户注册过
//        user.setMessage_num(user.getMessage_num()+1);  //变更发言数目
//        user.setScore(user.getScore()+10);  //变更积分
//        setValue(user.getId(),user);  //在redis中缓存user
//        return user;
//    }
}
