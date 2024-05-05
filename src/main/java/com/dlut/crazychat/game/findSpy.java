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
    @Value("${game.findSpy.spyNum}")
    private int spyNum;  //卧底的人数
    @Value("${game.findSpy.rewardOneturn}")
    private int rewardOneturn;
    @Value("${game.findSpy.rewardWinnerSpy}")
    private int rewardWinnerSpy;
    @Value("${game.findSpy.rewardWinnerPeople}")
    private int rewardWinnerPeople;
    private HashMap<String,String> words=new HashMap<>();  //词组
    private List<String> keys;
    private List<String> values;
    private String gameWord;   //当前的词语
    private HashMap<String,userStat> users_gaming=new HashMap<>();   //游戏中的用户
    private HashMap<String,userStat> users_waiting=new HashMap<>();   //等待中的用户
    private HashMap<String,Boolean> role=new HashMap<>();
    private HashMap<String,Boolean> isVoted=new HashMap<>(); //记录用户是否投票
    private HashMap<Integer,String> num2ID;  //编号到用户id的映射
    private HashMap<String,Integer> ID2num;  //id到编号的映射
    private HashMap<String,Integer> vote_num=new HashMap<>(); //投票结果表
    private int gameStatus;  //游戏运行状态，0：待开始，1：游戏进行（发言阶段），2投票阶段，
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
    public void resetOneTurn(){
        //每轮重置一轮状态
        for(String userid:isVoted.keySet()){
            isVoted.put(userid,false);
        }
        vote_num.clear();


    }
    public void resetOneGame(){
        //一局重置一轮状态
        users_waiting.putAll(users_gaming);  //将所有正在运行的玩家置入等待列表中
        users_gaming.clear();
        isVoted.clear();
        num2ID.clear();
        ID2num.clear();
        role.clear();

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
        users_waiting.put(user.getId(),user);  //加入等待队列
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
            int code=200;  //启动失败
            result.put("code",code);
            result.put("info","游戏启动失败，已经有正在运行的游戏\n");
        }
        else if(users_waiting.size()<4){
            //队列人数不足
            int code=200;  //启动失败
            result.put("code",code);
            result.put("info","游戏启动失败，游戏人数不足四人\n");
        }
        else{
            //游戏处于等待中
            gameStatus=1;   //更改游戏状态
            boolean isInWaiting=false;   //发起启动游戏的用户是否在等待队列中
            users_gaming.clear();  //创建新的游戏用户队列
            users_gaming.putAll(users_waiting);  //将等待队列中的所有用户添加进游戏中的用户map中
            if(!users_waiting.containsKey(user.getId())){
                users_gaming.put(user.getId(),user);
            }
            users_waiting.clear();   //清空等待队列
            rellocation();  //分配角色与游戏词语
            int code=400;  //成功启动游戏
            result.put("code",code);
            result.put("info","游戏启动成功！！！\n");
        }
        return result;
    }
    public void endGame(){
        //结束正在进行的游戏
        gameStatus=0;
        resetOneGame();  //结束一个游戏
    }
    public void rellocation(){
        //分配角色和词语和编号，完成初始化工作
        gameWord=getWord();   //分配当局的游戏词语
        num2ID.clear(); //重置
        ID2num.clear();
        //重置所有人的角色
        for(String key:role.keySet()){
            role.put(key,false);
        }
        List<String> candidate=new ArrayList<>();
        int count=1;
        //只生成一个spy
        for(String key:users_gaming.keySet()){
            num2ID.put(count,key);  //编号
            ID2num.put(key,count);
            count++;
            candidate.add(key);
        }
        Collections.shuffle(candidate);
        for(int i=0;i<spyNum;i++){
            role.put(candidate.get(i),true);  //设置为spy
        }
    }
    public boolean vote(userStat usr,int num){
        if(!users_gaming.containsKey(usr.getId())) return false;  //该用户没在正在进行的游戏中,投票失败
        if(num<=0||num>users_gaming.size()){
            isVoted.put(usr.getId(),true);  //标记为已投票
            return false;  //投票无效的号码，投票失败
        }
        String usr_id=num2ID.get(num);  //根据编号获取用户id
        vote_num.put(usr_id,vote_num.getOrDefault(usr_id,0)+1);  //为对应的用户投票
        isVoted.put(usr.getId(),true);
        return true;
    }

    public String oneTurnResult(){
        //计算一轮的结果
        int max_vote_num=0;
        String out_id="";  //出局的用户id
        //计算投票的最大数目
        for(String usr_id:users_gaming.keySet()){
            //遍历所有用户的id
            if(max_vote_num<vote_num.getOrDefault(usr_id,0)){
                max_vote_num=vote_num.get(usr_id);
                out_id=usr_id;
            }
        }
        //提出该玩家
        out_player(out_id);
        if(gameIsOver()){
            //游戏结束
            return "gameover";
        }
        else{
            //游戏继续
            return "goingon";
        }

    }
    public void out_player(String user_id){
        //踢出该玩家
        users_waiting.put(user_id,users_gaming.get(user_id));
        users_gaming.remove(user_id);
        isVoted.remove(user_id);
        vote_num.remove(user_id);
    }
    public boolean gameIsOver(){
        //判别游戏是否结束
        int spy_num=0;
        int common_num=0;
        for(String usr_id:users_gaming.keySet()){
            if(role.get(usr_id)==true){
                //是卧底
                spy_num++;
            }
            else{
                common_num++;
            }
        }
        if(spy_num==0||common_num<=spy_num) return true;  //游戏结束
        return false;
    }
    public Map<String,Object> getUserRole(userStat user){
        //获取用户信息，包括词语以及其编号
        HashMap<String,Object> result=new HashMap<>();
        int num=ID2num.get(user.getId());
        result.put("no",num);
        boolean isSpy=role.get(user.getId());
        if(isSpy){
            result.put("spy",true);
            String word=words.get(gameWord);  //卧底的词语
            result.put("word",word);
        }
        else{
            result.put("spy",false);
            result.put("word",gameWord);
        }
        return result;
    }
    public String getAllUserRole(){
        //获取用户信息，包括词语以及其编号
        StringBuilder result=new StringBuilder();
        for(String userId:role.keySet()){
            int num=ID2num.get(userId);
            result.append("编号："+num+"\t");
            boolean isSpy=role.get(userId);
            if(isSpy){
                result.append("身份： 卧底\t");
                String word=words.get(gameWord);  //卧底的词语
                result.append("词语: "+word+"\t");
            }
            else{
                result.append("身份： 平民\t");
                result.append("词语："+gameWord+"\t");
            }
            result.append("\n");
        }

        return result.toString();
    }
    public String set2VoteStage(userStat user){
        //设置为投票阶段
        if(gameStatus!=1){
            //游戏未处于发言阶段
            return "发言阶段未结束，请结束后再开启投票阶段。\n";
        }
        gameStatus=2;  //置于投票阶段
        return "投票阶段已开启，请输入#vt n进行投票，n为需要票出的玩家编号。\n";
    }
    public String set2SpeakStage(userStat user){
        //设置为发言阶段
        gameStatus=1;
        return "发言阶段开始，请玩家输入#spk xxx。进行发言，系统会自动匿名对消息进行显示。\n";
    }


//    public static void main(String[] args) {
//        HashMap<String,String> mp=new HashMap<>();
//        mp.put("baidu","baidu");
//        mp.put("tencent","baidu");
//        mp.put("huawei","baidu");
//        mp.put("ali","baidu");
//        Set<String> st=new HashSet<>();
//        st.add("ali");
//        st.add("baidu");
//        st.add("tencent");
//        st.add("bili");
//        st.add("bytedance");
//        Collections.shuffle(st);
//        for(String key:mp.keySet()){
//            System.out.println(key);
//        }
//    }
}
