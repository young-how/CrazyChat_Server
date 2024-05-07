package com.dlut.crazychat.service;

import com.dlut.crazychat.game.dailySign;
import com.dlut.crazychat.game.findSpy;
import com.dlut.crazychat.game.guessNum;
import com.dlut.crazychat.game.lottery;
import com.dlut.crazychat.pojo.rankList;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.utils.SystemManager;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Data
@ToString
public class gameService {
    @Autowired
    private userService userservice;
    @Autowired
    private guessNum guessnum;  //猜数字游戏
    @Autowired
    private lottery lt;   //彩票游戏
    @Autowired
    private dailySign sign;  //每日签到
    @Autowired
    private findSpy findspy;
    private String gameNamePlaying="None";  //正在进行的游戏
    @Autowired
    private SystemManager manager;
//    @Autowired
//    public void setManager(SystemManager systemManager){
//        manager=systemManager;
//    }
    public String process(String user_id,String command){
        userStat user=userservice.findUserByID(user_id);
        //处理从消息队列来的消息，并返回处理信息
        if(command.contains("#stat")){
            //返回用户状态
            //userStat user=userservice.findUserByID(user_id);
            String info="\n用户名:"+user.getName()+"  发言数:"+user.getMessage_num()+"  积分:"+user.getScore()+" 等级:"+user.getLevel()+" 排名:"+user.getRank()+"\n";   //构造返回信息
            //manager.send(info);
            return info;
        }
        else if(command.contains("#rank")||command.contains("#rk")){
            rankList rank=userservice.getRankList();  //获取所有用户的排名
            StringBuilder info=new StringBuilder("\n********************积分排行榜********************\n");
            info.append("排名\t用户名\t积分\t等级\n");
            for(userStat user_rk: rank.getUsers()){
                info.append(user_rk.getRank()+"\t"+user_rk.getName()+"\t"+user_rk.getScore()+"\t"+user_rk.getLevel()+"\n");
            }
            info.append("********************积分排行榜********************"+"\n");
            //manager.send(info.toString());
            return info.toString();
        }
        else if(command.contains("#lt")){
            if(command.contains("help")){
                return lt.explain();
            }
            //userStat user=userservice.findUserByID(user_id);
            String regex = "#lt\\s*(\\d+)";
            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex);
            // 创建匹配器对象
            Matcher matcher = pattern.matcher(command);  //解析命令
            StringBuilder info=new StringBuilder();
            // 查找匹配的数字
            if (matcher.find()) {
                // 获取匹配到的数字字符串
                String numberStr = matcher.group(1);
                int num=Integer.parseInt(numberStr);
                Map<String,Integer> re=lt.buy(num); //买num张彩票
                int earn=0;
                String information="";
                for(String s:re.keySet()){
                    information=s;
                    earn=re.get(s);
                }
                info.append(information+"\n");  //加入信息
                userservice.addScore(user,earn);  //添加赚取的钱
            }
            else{
                Map<String,Integer> re=lt.buy(1); //买1张彩票 默认买一张
                int earn=0;
                String information="";
                for(String s:re.keySet()){
                    information=s;
                    earn=re.get(s);
                }
                info.append(information+"\n");  //加入信息
                userservice.addScore(user,earn);  //添加赚取的钱
            }
            info.append("当前奖池: "+lt.getBonus()+"\n");
            return info.toString();
        }
        else if(command.contains("#gs")){
            if(command.contains("help")){
                return guessnum.explain();  //解释游戏规则
            }
            //userStat user=userservice.findUserByID(user_id);
            String regex = "#gs\\s*(\\d+)";
            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex);
            // 创建匹配器对象
            Matcher matcher = pattern.matcher(command);  //解析命令
            StringBuilder info=new StringBuilder("\n********************猜词游戏(0-1000的数)********************\n");
            // 查找匹配的数字
            if (matcher.find()) {
                // 获取匹配到的数字字符串
                String numberStr = matcher.group(1);
                int num=Integer.parseInt(numberStr);
                int guessResult=guessnum.guess(num);
                if(guessResult==1){
                    //猜大了
                    user=userservice.addScore(user,-guessnum.getMoney_per_time());  //扣分
                    info.append(user.getName()+"\t猜的:"+num+"结果:猜大了\t"+"奖池累积:"+guessnum.getBonus()+"\n");
//                    info.append(user.getName()+"\t猜的:"+num+"\t结果:猜大了\n");
//                    info.append("消耗积分:"+guessnum.getMoney_per_time()+"\t剩余积分:"+user.getScore()+"\n");  //结果
//                    info.append("奖池累积:\t"+guessnum.getBonus()+"\n");
                }
                else if(guessResult==-1){
                    //猜小了
                    user=userservice.addScore(user,-guessnum.getMoney_per_time());  //扣分
                    info.append(user.getName()+"\t猜的:"+num+"结果:猜小了\t"+"奖池累积:"+guessnum.getBonus()+"\n");
                    //info.append("消耗积分:"+guessnum.getMoney_per_time()+"\t剩余积分:"+user.getScore()+"\n");  //结果
                    //info.append("奖池累积:\t"+guessnum.getBonus()+"\n");
                }
                else{
                    //
                    user=userservice.addScore(user,guessResult);  //加分
                    info.append(user.getName()+"\t猜的:\t"+num+"\n");
                    info.append("结果:\t猜中了!!!。\t获得奖励:"+guessResult+"\t剩余积分:"+user.getScore()+"\n");  //结果
                    guessnum.resetTarget();  //重置奖励
                }
            }
            info.append("***************************************************************\n");
            return info.toString();
        }
        else if(command.contains("#qd")){
            //签到功能
            StringBuilder info=new StringBuilder(user.getName()+"的签到结果:");
            LocalDate toDay= LocalDate.now(); //当前的日期
            Map<String,Object> re=sign.doSign(user_id,toDay.toString());  //签到
            if(re.get("code").equals(Integer.valueOf(400))){
                //已经签到
                info.append("很抱歉你今天已经签到过了!!!\n");
            }
            else if(re.get("code").equals(Integer.valueOf(200))){
                //签到成功
                info.append("签到成功!!!获取的奖励如下:\n");
                Integer sign_sum=(Integer)(re.get("continuous"));   //连续签到的天数
                Long sign_count=(Long)re.get("count");   //签到的总数
                Random rand=new Random();  //随机数生成器
                double rand_num= rand.nextDouble();  //随机生成的小数
                int reward=(int)re.get("reward");   //得到生成的奖励
                userservice.addScore(user,reward);  //更新奖励
                info.append("签到奖励:"+reward+"  签到天数:"+sign_count+"  连续签到天数:"+sign_sum+"\n");
            }
            return info.toString();  //返回签到结果
        }
        else if(command.contains("#help")){
            StringBuilder info=new StringBuilder("\n********************服务器指令列表********************\n");
            info.append("#st #stat\t说明：显示自身用户的信息\n");
            info.append("#lt \t说明：购买彩票，单次消耗30积分\n");
            info.append("#rk \t说明：显示用户排名\n");
            info.append("#gs n \t说明：猜词游戏，猜中数字n即可获得奖励\n");
            info.append("#qd \t说明：每日签到,随机生成一个(0-1)的随机数x，签到奖励=(1000+10*签到天数+30*连续签到天数)/(x+0.1)\n");
            return info.toString();
        }
        else if(command.contains("#game")){
            StringBuilder info=new StringBuilder("\n********************游戏列表********************\n");
            info.append("findSpy\t说明：输入#join findSpy加入游戏队列\n");
            return info.toString();
        }
        else if(command.contains("#join")){
            //包含加入游戏等待队列的命令
            String input = command;
            String pattern = "#join\\s+(\\w+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(input);
            if (m.find()) {
                String gamename = m.group(1);
                if(gamename.equals("findSpy")){
                    if(gameNamePlaying!="None"&&gameNamePlaying!="findSpy") return gameNamePlaying+"正在运行中，请输入#end "+gameNamePlaying+",结束后再输入!\n";
                    if(gameNamePlaying.equals("None")){
                        gameNamePlaying="findSpy";
                        findspy.setGameStatus(0);
                    }
                    String info=findspy.join(user);  //加入用户
                    return info;
                }
            } else {
                return "请输入正确的游戏名\n";
            }
        }
        else if(command.contains("#start")){
            //开始游戏的命令
            String input = command;
            String pattern = "#start\\s+(\\w+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(input);
            if(gameNamePlaying.equals("None")) return "当前没有正在组队的游戏";
            if(gameNamePlaying.equals("findSpy")){
                String info="";
                Map<String,Object> re=findspy.startGame(user);  //加入用户
                if(re.get("code").equals(200)){
                    //游戏启动失败
                    info=(String) re.get("info");
                    return info;
                }
                else{
                    //启动成功
                    Map<String,String> roles=(Map<String,String>)re.get("roles");  //获取所有的角色信息
                    for(String id:roles.keySet()){
                        manager.send(roles.get(id),id);  //为特定用户发送私密消息
                        //manager.send(roles.get(id));  //为所有用户发送消息，仅测试使用
                    }
                }
                return "游戏启动成功，系统为每位用户发送了私密信息，请查收！\n";
            }
            return "当前游戏名称有误";
        }
        else if(command.contains("#end")){
            //结束某游戏的命令
            String input = command;
            String pattern = "#end\\s+(\\w+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(input);
            if (m.find()) {
                String gamename = m.group(1);
                if(gamename.equals("findSpy")){
                    gameNamePlaying="None";
                    String info=findspy.endGame();  //结束游戏
                    return info;
                }
            } else {
                return "请输入正确的游戏名\n";
            }
        }
        else if(command.contains("#vote")){
            //给某个编号的用户或者id进行投票
            String input = command;
            String pattern = "\\d+";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(input);
            if (m.find()) {
                String num = m.group(0);  //投票的编号
                if(gameNamePlaying.equals("findSpy")){
                    Map<String,String> vote_result=findspy.vote(user,Integer.parseInt(num));
                    String info=vote_result.get("info_vote"); //获取投票结果
                    if(vote_result.containsKey("end")){
                        //当前游戏已结束，结算奖励
                        vote_result.remove("end");
                        vote_result.remove("info_vote");
                        String add_info=(String)vote_result.get("info"); //额外的结算信息
                        vote_result.remove("info");
                        info=info+add_info;
                        for(String id:vote_result.keySet()){
                            int reward=Integer.parseInt(vote_result.get(id));  //获取奖励值
                            try{
                                userStat addsocore_user=new userStat();
                                addsocore_user.setId(id);
                                userservice.addScore(addsocore_user,reward);//对相应的用户进行加分
                            }
                            catch (RuntimeException e){
                                e.printStackTrace();
                            }
                            finally {
                                continue;
                            }

                        }
                        return info;
                    }

                    return info;
                }
            } else {
                return "没有正在进行投票的游戏\n";
            }
        }
        return "命令错误，请检查指令\n";
    }
}
