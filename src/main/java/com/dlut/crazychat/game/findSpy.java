package com.dlut.crazychat.game;

import com.dlut.crazychat.pojo.userStat;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Data
@Component
public class findSpy {
    @Value("${game.findSpy.dictionary}")
    private String dictionaryPath;   //词典文件名称
    private HashMap<String,String> words=new HashMap<>();  //词组
    private List<String> keys;
    private List<String> values;
    private Queue<userStat> users_gaming=new LinkedList<>();   //游戏中的用户
    private Queue<userStat> users_waiting=new LinkedList<>();   //等待中的用户
    private int gameStatus;  //游戏运行状态，0：待开始，1：正在进行
    @PostConstruct
    public void init(){
        //构造字典
        String line;
        String key;
        String value;
        try(BufferedReader br=new BufferedReader(new FileReader(dictionaryPath))){
            while((line=br.readLine())!=null){
                String[] data=line.split(",");
                key=data[0];
                value=data[1];
                words.put(key,value);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        keys=new ArrayList<>(words.keySet());
        values=new ArrayList<>(words.values());
    }
    /*
     * @param null:
      * @return null
     * @author younghow
     * @description 随机获取一局游戏需要的词语
     * @date younghow younghow
     */
    public String getWord(){
        Random random = new Random();
        int index=random.nextInt(keys.size());
        String word=keys.get(index);
        return word;
    }
    /*
     * @param user: 用户信息
      * @return int
     * @author younghow
     * @description 一名用户加入游戏
     * @date younghow younghow
     */
    public int join(userStat user){
        users_waiting.offer(user);  //等待队列中加入用户
        return 1;
    }
    /*
     * @param user:发起开始游戏命令的用户
      * @return Map<String,Object>: 包含"code"状态码，用于标识游戏是否被成功开启；"info"状态信息，标识游戏的开启状态。
     * @author younghow
     * @description 开始游戏，若不满足游戏条件，返回错误信息。
     * @date younghow younghow
     */
    public Map<String,Object> startGame(userStat user){
        Map<String,Object> result=new HashMap<>();
        if(gameStatus==1){
            //游戏已开启
        }
        else{
            //游戏处于等待中
            gameStatus=1;   //更改游戏状态
            boolean isInWaiting=false;   //发起启动游戏的用户是否在等待队列中
            users_gaming=new LinkedList<>();  //创建新的游戏用户队列
            while(!users_waiting.isEmpty()){
                userStat peek=users_waiting.poll();
                if(peek.getId()==user.getId()) isInWaiting=true;
                users_gaming.offer(peek);

            }
        }
        return result;
    }
}
