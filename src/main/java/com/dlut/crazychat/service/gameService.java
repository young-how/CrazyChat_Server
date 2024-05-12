package com.dlut.crazychat.service;

import com.dlut.crazychat.game.*;
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
    @Autowired
    private Ollama_robot robot;
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
            String info="\n用户名:"+user.getName()+"  id号:"+user.getId()+"  发言数:"+user.getMessage_num()+"  积分:"+user.getScore()+" 等级:"+user.getLevel()+" 排名:"+user.getRank()+"\n";   //构造返回信息
            //manager.send(info);
            return info;
        }
        else if(command.contains("#rank")||command.contains("#rk")){
            rankList rank=userservice.getRankList();  //获取所有用户的排名
            StringBuilder info=new StringBuilder("\n********************积分排行榜********************\n");
            info.append("排名\t用户名\t积分\t等级\n");
            long count=1;
            for(userStat user_rk: rank.getUsers()){
                info.append(count+"\t"+user_rk.getName()+"\t"+user_rk.getScore()+"\t"+user_rk.getLevel()+"\n");
                user_rk.setRank(count);
                userservice.find_Update_User(user_rk);  //更新每个用户的排名
                count++;
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
                int num=Integer.parseInt(numberStr);  //彩票的数量
                int remain_money=user.getScore(); //用户剩余分数
                if(remain_money<num*lt.getPrize()){
                    //剩下的分数不足以买彩票
                    return "你的积分不够买彩票哦，攒点钱再来吧\n";
                }
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
            info.append("#give \t说明：赠予其他玩家积分，使用格式为#give id n。其中id为输入#stat后得到的唯一id，n为赠送的积分数目。\n");
            info.append("#ask \t说明：向AI机器人提问，AI机器人会尽可能满足你的要求(警告：机器人很暴躁！！！)。\n");
            info.append("#@ \t说明：向AI机器人提问，AI机器人会私信回复你（相应速度快，生成大量文本建议使用该模式）。\n");
            info.append("#join findSpy \t说明：加入寻找卧底游戏的等待队列中，当人数集齐后输入#start开始游戏。" +
                    "游戏说明：游戏开始后系统会给每个玩家发一个红色字体的私密信息，包含编号和自己的词语，游戏开始后所有玩家将名称改为对应的号码。" +
                    "当所有玩家描述完后输入#vote n投票给n号玩家。票数最多的玩家将会被踢出游戏，无法进行投票。直到所有卧底被找出或者游戏人数中的平民玩家小于卧底人数\n");
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
        else if(command.contains("#give")){
            // 匹配 MAC 地址的正则表达式
            String macRegex = "(?i)([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})";
            // 编译正则表达式
            Pattern macPattern = Pattern.compile(macRegex);
            String macAddress="";
            // 匹配 MAC 地址
            Matcher macMatcher = macPattern.matcher(command);
            if (macMatcher.find()) {
                macAddress = macMatcher.group();
                //System.out.println("MAC 地址: " + macAddress);
            } else {
                //System.out.println("未找到 MAC 地址。");
                return "id号格式错误\n";
            }
            //判别用户是否存在
            if(userservice.findUserByID(macAddress)==null){
                return "对应的用户不存在\n";
            }
            // 匹配赠予的积分数
            userStat gived_user=userservice.findUserByID(macAddress);  //找到对应的用户
            String numberRegex = macAddress+" (\\d+)";
            Pattern numberPattern = Pattern.compile(numberRegex);
            Matcher numberMatcher = numberPattern.matcher(command);
            int give_num=0;
            if (numberMatcher.find()) {
                String number = numberMatcher.group(1);
                give_num=Integer.parseInt(number);  //赠送的积分数目
                if(give_num<0) return "你很坏嘛，程序虽然bug多，但别想着偷人家钱哦~\n";
                if(user.getScore()<give_num) return "你口袋里的积分貌似不够送人家哦~\n";

                userservice.addScore(gived_user,give_num);
                userservice.addScore(user,-give_num);   //bug，还需要保证事务的一致性
            } else {
                return "你输入的积分数目有误\n";
            }
            return user.getName()+" 赠送了玩家:"+ gived_user.getName()+" "+give_num+" 积分\n";
        }
        else if(command.contains("#ask")){
            //询问ollama机器人
            String input = command.replace("#ask","");
            robot.askOllama(input);  //询问机器人问题，机器人回复
            return user.getName()+"？，我tm已经听到了，别走啊！等我一会。";
        }
        else if(command.contains("#@")){
            //流式询问ollama机器人
            String input = command.replace("#@","");
            robot.askOllamaByStream(input,user);  //询问机器人问题，机器人回复(私密发送)
            return "";
        }
        return "命令错误，请检查指令\n";
    }
}
