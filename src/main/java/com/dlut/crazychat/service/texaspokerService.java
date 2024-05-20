package com.dlut.crazychat.service;

import com.dlut.crazychat.pojo.pokerDesk;
import com.dlut.crazychat.pojo.texasPlayer;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.utils.PokerUtils;
import com.dlut.crazychat.utils.pokerScoring;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/*
提供一个德州扑克房间
 */
@Service
@Data
public class texaspokerService {
    //private Map<String, List<userStat>> rooms = new HashMap<>(); // 房间ID到用户列表的映射
    private volatile Deck deck=new Deck(); // 房间牌堆
    private volatile List<texasPlayer> room =new ArrayList<>(); // 房间ID到玩家列表的映射
    private volatile HashMap<String,userStat> userInGame=new HashMap<>();   //房间中中的用户id和用户实体的映射
    private  int currentPlayerIndex=0; // 当前玩家索引
    private volatile Integer highestBet=0; // 当前最高下注金额
    private volatile Integer pot=0; // 奖池金额
    private volatile Integer currentTurn=0;  //当前下注轮次
    private volatile List<String> boardCards=new ArrayList<>(); //场面牌
    private volatile List<texasPlayer> winner=new ArrayList<>();  //赢家
    private volatile List<texasPlayer> second_winner=new ArrayList<>();  //池边赢家
    private volatile boolean started;  //游戏是否已经启动
    private volatile Map<String,Integer> currentUserAction=new HashMap<>();  //当前玩家的动作
    private ThreadPoolExecutor pool=new ThreadPoolExecutor(5,5,60L, TimeUnit.SECONDS,new LinkedBlockingQueue<>(5));
    @Autowired
    private pokerScoring pokerscoring;  //用于统计得分的工具类
    @PostConstruct
    public void init(){
        listenUserBet();  //异步监听当前用户的操作
    }
    /*
    保障线程同步
     */
//    public synchronized void setCurrentPlayerIndex(int index) {
//        currentPlayerIndex = index;
//    }
//    public synchronized int getCurrentPlayerIndex() {
//        return currentPlayerIndex;
//    }
//    public synchronized void setStarted(boolean started) {
//        this.started = started;
//    }
//    public synchronized boolean getStarted() {
//        return this.started;
//    }
//    public synchronized void setCurrentUserAction(Map<String,Integer> currentUserAction) {
//        this.currentUserAction = currentUserAction;
//    }
//    public synchronized Map<String,Integer> getCurrentUserAction() {
//        return this.currentUserAction;
//    }
    public boolean joinRoom(userStat user,int init_money) {
        if(user==null) return false;  //空用户加入失败
        if(!userInGame.containsKey(user.getId())){
            //玩家已经退出
            userInGame.put(user.getId(),user);
            texasPlayer player=new texasPlayer(user);
            player.setMoney(init_money);
            if(started){
                //游戏已开始
                player.setFolded(true);  //默认弃牌
            }
            boolean inRoom=false;  //玩家是否还在房间的玩家列表中
            //如果玩家还在房间里，join变为充钱逻辑
            for(texasPlayer playerInGaming:room){
                if(playerInGaming.getId().equals(user.getId())){
                    //playerInGaming.setFolded(true);
                    playerInGaming.setLeaved(false);
                    playerInGaming.setMoney(playerInGaming.getMoney()+init_money);  //充钱
                    playerInGaming.setUser(user);
                    inRoom=true;
                }
            }
            if(!inRoom)  room.add(player);  //该用户不在房间中，才加入用户列表
            return true;
        }else{
            //玩家未退出，知识兑换筹码
            for(texasPlayer playerInGaming:room){
                if(playerInGaming.getId().equals(user.getId())){
                    //playerInGaming.setFolded(true);
                    playerInGaming.setLeaved(false);
                    playerInGaming.setMoney(playerInGaming.getMoney()+init_money);  //充钱
                    playerInGaming.setUser(user);
                    return true;
                }
            }
        }
        return false;
    }
    /*
    返回某个用户的序号
     */
    public int getNo(userStat user){
        int re=-1;
        for(int i=0;i<room.size();i++){
            if(room.get(i).getId().equals(user.getId())) return i;
        }
        return re;
    }
    /*
    退出房间，并将筹码换为积分
     */
    public int exitRoom(userStat user){
        int reward=0;
        int indx=0;
        if(!userInGame.containsKey(user.getId())) return 0;
        for(texasPlayer player:room){
            if(player.isLeaved()) continue;
            if(player.getId().equals(user.getId())){
                reward=player.getMoney();
                player.setMoney(0);
                player.setFolded(true);  //弃牌
                player.setLeaved(true);  //设置为以离开
                break;
            }
            indx++;
        }
        if(!started){
            room.remove(indx);  //游戏没有启动，直接删除
        }
        userInGame.remove(user.getId());  //删除用户信息
        return reward;
    }
    /*
    根据用户请求返回牌桌信息
     */
    public pokerDesk getDeskInfo(userStat user) throws CloneNotSupportedException {
        pokerDesk desk=new pokerDesk();
        desk.setPot(pot);
        desk.setCurrentUser_id(currentPlayerIndex);
        desk.setStarted(started);  //是否开始游戏
        desk.setCurrentHighestBet(highestBet);  //设置当前下注金额
        desk.setRound(currentTurn);  //设置当前下注轮次
        if(winner.size()>0){
            StringBuilder re=new StringBuilder();
            for(texasPlayer pl:winner){
                re.append(getNo(pl.getUser())+"号玩家 ");
                desk.setWinner_cards(pl.getHand());  //设置赢家手牌
            }
            desk.setWinner(re.toString());
        }


        //根据轮次设置用户可见的桌面牌
        if(boardCards!=null&&boardCards.size()==5){
            if(currentTurn==0&&started){
                //第一轮下注
                desk.setBoardCards(new ArrayList<>());  //桌面牌是空的
            }
            else if(currentTurn==1){
                //第二轮下注
                desk.setBoardCards(new ArrayList<>());  //桌面牌是空的
                desk.getBoardCards().add(boardCards.get(0));
                desk.getBoardCards().add(boardCards.get(1));
                desk.getBoardCards().add(boardCards.get(2));
            }
            else if(currentTurn==2){
                //第3轮下注
                desk.setBoardCards(new ArrayList<>());
                desk.getBoardCards().add(boardCards.get(0));
                desk.getBoardCards().add(boardCards.get(1));
                desk.getBoardCards().add(boardCards.get(2));
                desk.getBoardCards().add(boardCards.get(3));
            } else if(currentTurn>=3||!started){
                desk.setBoardCards(new ArrayList<>());
                desk.getBoardCards().add(boardCards.get(0));
                desk.getBoardCards().add(boardCards.get(1));
                desk.getBoardCards().add(boardCards.get(2));
                desk.getBoardCards().add(boardCards.get(3));
                desk.getBoardCards().add(boardCards.get(4));
            }
        }

        for(int i=0;i<room.size();i++){
            texasPlayer player=(texasPlayer) room.get(i).clone();
            player.setStatics(pokerscoring.getPokerStaticsByID(player.getId()));  //返回胜率信息等
            player.setNo(i);  //设置在序列中的编号
            List<String> hand=new ArrayList<>();
            if(started==false&&!player.getId().equals(user.getId())){
                //游戏结算时,其他玩家的牌型可见
                hand.addAll(player.getHand());  //将手牌可见
            }
            if(player.getId().equals(user.getId())){
                hand.addAll(player.getHand());  //将手牌可见
                desk.setHadCards(player.getHand());
                desk.setMoney(player.getMoney());
                desk.setOwn_id(i);  //设置自己的序号
                if(started) player.setCardLevel(null);  //牌型不可见
            }
            else if(started){
                player.setCardLevel(null);
            }
            player.setHand(hand);
            desk.getUsers().add(player);
        }
        return desk;
    }
    public void startGame() {
        deck.shuffle();
        started=true;
        currentTurn=0;  //重置当前下注的轮次
        setCurrentPlayerIndex(0);  //最开始的玩家设置为0号
        //重置所有玩家状态
        resetAllUser();
        // 分配决策顺序，发牌等逻辑
        int playerCount=room.size();
        Map<String, Object> result = dealCards(playerCount);
        boardCards=(List<String>) result.get("boardCards");
        List<List<String>> playersHands = (List<List<String>>) result.get("playersHands"); //玩家手牌
        for(int i=0;i<playerCount;i++){
            room.get(i).setHand(playersHands.get(i));  //设置手牌
        }
    }
    /*
    监听玩家下注情况
     */
    public void listenUserBet(){
        Runnable task=()->{
            while (true){
                try {
                    nextBettingRound();
                    sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    continue;
                }
            }
        };
        try {
            pool.execute(task);
        }
        catch (RuntimeException e){
            e.printStackTrace();
            //pool.execute(task);
        }
    }
    /*
    重置所有玩家状态
     */
    public void resetAllUser(){
        for(texasPlayer player:getRoom()){
            if(player.isLeaved()){
                getRoom().remove(player);  //移除该玩家
                continue;
            }
            player.resetGame();
        }
    }
    /*
    结算最终赢家
     */
    public void Settlement(){
        winner.clear(); //清除上次的赢家信息
        second_winner.clear();  //清除池边赢家
        choseWinner();  //计算赢家和池边赢家
        allocatePot(); //分配筹码
        System.out.println("所有玩家的信息");
        for(texasPlayer player:getRoom()){
            System.out.println("玩家id"+player.getId()+"  玩家剩余积分:"+player.getMoney());
        }
        System.out.println("奖池剩余:"+pot);
        started=false;
    }
    /*
    牌型比较
     */
    public void choseWinner(){
        for(texasPlayer player:getRoom()){
            PokerUtils.Hand gamer=PokerUtils.evaluateBestHand(player.getHand(),boardCards);  //当前玩家场面牌+手牌
            player.setCardLevel(gamer.rank.toString());  //设置玩家的手牌等级
            System.out.println("玩家id"+player.getId()+"  玩家牌型:"+gamer.rank+" 手牌:"+player.getHand()+" 场面牌:"+boardCards);
            if(player.isFolded()) continue;  //弃牌玩家不参与结算
            if(winner.size()==0){
                winner.add(player);
            }
            else{
                //PokerUtils.Hand gamer=PokerUtils.evaluateBestHand(boardCards,boardCards);  //评估场面牌+手牌
                PokerUtils.Hand winer_hand=PokerUtils.evaluateBestHand(winner.get(0).getHand(),boardCards);  //评估场面牌+手牌
                if(gamer.compareTo(winer_hand)==1){
                    second_winner.clear();
                    second_winner.addAll(winner);  //更新池边赢家
                    winner.clear();
                    winner.add(player);  //更新赢家
                }
                else if(gamer.compareTo(winer_hand)==-1){
                    //与池边玩家相比较
                    if(second_winner.size()==0){
                        second_winner.add(player);
                    }else{
                        PokerUtils.Hand second_winer_hand=PokerUtils.evaluateBestHand(second_winner.get(0).getHand(),boardCards);  //评估场面牌+手牌
                        if(gamer.compareTo(second_winer_hand)==1){
                            second_winner.clear();
                            second_winner.add(player);  //更新边池赢家
                        }
                        else if(gamer.compareTo(second_winer_hand)==0){
                            second_winner.add(player);
                        }
                    }
                }
                else{
                    //牌型相等
                    winner.add(player);
                }
            }
        }
    }
    /*
    奖池积分结算，并将得分信息统计到redis
     */
    public void allocatePot(){
        //结算每个赢家的积分
        int moneyAdd=pot/ winner.size();  //每个赢家的奖池积分
        for(texasPlayer player:winner){
            if(player.getMoney()==0){
                //该玩家发生了showhand行为
                int reward=Math.min(player.getCurrentGameBet()*room.size(),moneyAdd); //计算showhand后的奖励
                pot-=reward;
                player.setMoney(reward);
                pokerscoring.addPokerNum(player.getId(),true,reward-player.getCurrentGameBet(),player.getCardLevel()); //统计赢家信息
            }else{
                player.addMoney(moneyAdd);  //获得金钱奖励
                pot-=moneyAdd;  //奖池减少
                pokerscoring.addPokerNum(player.getId(),true,moneyAdd-player.getCurrentGameBet(),player.getCardLevel()); //统计赢家信息
            }
        }
        boolean secondGamer_win=false;  //第二大的玩家是否分配到奖励
        if(pot>0){
            //奖池还有剩余，池边赢家瓜分
            secondGamer_win=true;
            int moneyAdd_second=pot/ second_winner.size();  //每个赢家的奖池积分
            for(texasPlayer player:second_winner){
                if(player.getMoney()==0){
                    //该玩家发生了showhand行为
                    int reward=Math.min(player.getCurrentGameBet()*room.size(),moneyAdd_second); //计算showhand后的奖励
                    pot-=reward;
                    player.setMoney(reward);
                    pokerscoring.addPokerNum(player.getId(),true,reward-player.getCurrentGameBet(),player.getCardLevel()); //统计赢家信息
                }else{
                    player.addMoney(moneyAdd_second);  //获得金钱奖励
                    pot-=moneyAdd_second;  //奖池减少
                    pokerscoring.addPokerNum(player.getId(),true,moneyAdd_second-player.getCurrentGameBet(),player.getCardLevel()); //统计赢家信息
                }
            }
        }
        //统计其他没有获奖的玩家
        for(texasPlayer player:getRoom()){
            if(winner.contains(player)||(second_winner.contains(player)&&secondGamer_win)) continue;
            pokerscoring.addPokerNum(player.getId(),false,-player.getCurrentGameBet(),player.getCardLevel()); //统计非赢家信息
        }
    }
    /*
    当前用户下注
     */
    public boolean bet(int money){
        texasPlayer currentPlayer=room.get(currentPlayerIndex);
        if(money<highestBet&&currentPlayer.getMoney()!=0) return false;  //下注不满足条件
        currentUserAction.clear();
        currentUserAction.put("action",1);
        currentUserAction.put("money",money);
        return true;  //下注成功
    }
    /*
    当前用户弃牌
     */
    public boolean fold(){
        currentUserAction.clear();
        currentUserAction.put("action",-1);
        return true;  //弃牌成功
    }
    /*
    判断输入用户是否是当前应该执行操作的用户
     */
    public boolean isCurrentUser(userStat user){
        int no=getNo(user);  //获取当前用户的no号
        if(no==currentPlayerIndex) return true;
        else return false;
    }
    // 其他功能：下注、开牌、比较牌型、结算积分等
    /*
    异步监听用户的下注情况，然后进行一轮的推演
     */
    public void nextBettingRound() throws InterruptedException {
        while (true) {
            List<texasPlayer> players = getRoom();  //获取当前房间的玩家
            if (players == null || players.isEmpty()||!started) {
                return; //游戏未开始，没有玩家
            }
            int currentIndex = getCurrentPlayerIndex();  //获取当前正在进行操作的用户
            int currentHighestBet = getHighestBet();
            texasPlayer currentPlayer = players.get(currentIndex);
            if(checkEnd()){
                //游戏结束
                Settlement();  //结算
                return; //结束
            }
            // 跳过已弃牌的玩家
            if (currentPlayer.isFolded()) {
                setCurrentPlayerIndex((currentIndex + 1)% players.size());
                continue;
            }

            // 检查当前玩家是否需要跟注或加注，如果该玩家已经showhand，跳过
            if ((currentPlayer.hasActed() && currentPlayer.getCurrentBet() >= currentHighestBet)||currentPlayer.getMoney()==0) {
                setCurrentPlayerIndex((currentIndex + 1)% players.size());
            } else {
                // 等待当前玩家进行操作（例如下注、跟注、加注、弃牌等）
                // 这里可以添加逻辑来请求玩家进行操作
                // 例如：currentPlayer.act(currentHighestBet);
                // 假设玩家进行了一次操作，我们直接更新状态（实际情况应该是异步等待玩家操作）
                // 这里是示例代码
                Map<String,Integer> currentUserAction=getCurrentUserAction();
                if(currentUserAction.size()==0){
                    //当前的玩家没有采取动作，跳过
                    continue;
                } else if (currentUserAction.get("action").equals(-1)) {
                    //玩家弃牌操作
                    currentPlayer.setFolded(true);
                }
                else if(currentUserAction.get("action").equals(1)){
                    //玩家下注操作
                    int bet_money=currentUserAction.get("money");  //获取下注金额
                    int old_bet=currentPlayer.getCurrentBet();
                    currentPlayer.act(bet_money);
                    // 更新奖池
                    //pot=pot + currentPlayer.getCurrentBet()-old_bet;
                    setPot(getPot()+ currentPlayer.getCurrentBet()-old_bet); //保证线程同步
                    // 更新当前最高下注金额
                    if (currentPlayer.getCurrentBet() > currentHighestBet) {
                        //highestBet= currentPlayer.getCurrentBet();
                        setHighestBet(currentPlayer.getCurrentBet());
                        // 重置所有玩家的行动状态，让他们再次行动
                        resetPlayersAction(players);
                        currentPlayer.setActed(true);  //当前玩家不用重置
                    }
                    System.out.println(currentIndex+"号玩家已下注:"+currentPlayer.getCurrentBet()+"积分，当前奖池:"+pot);
                }
                getCurrentUserAction().clear();  //当前用户的下注信息。
                setCurrentPlayerIndex((currentIndex + 1) % players.size());  //动作执行完毕，切换为下一个玩家
            }
            // 检查是否所有玩家都已行动，并且当前玩家是按钮后的第一个玩家
            if (allPlayersActed(players, currentHighestBet)) {
                break;
            }
            sleep(20);
        }
        //一轮次的下注已经完毕，进行后续处理
        //currentPlayerIndex=currentIndex;
        setCurrentPlayerIndex(0);
        startNewTurn();  //开始新一轮的设置
        if(getCurrentTurn()==4){
            //最后一轮下注已完成
            Settlement();  //结算奖励
            return;
        }
    }
    /*
    开始新一轮的下注
     */
    public void startNewTurn(){
        //highestBet=0;  //重置该轮下注的金额
        setHighestBet(0);
        //currentTurn=(currentTurn+1)%4;  //进入下一轮次的下注
        setCurrentTurn((getCurrentTurn()+1)%5);
        for(texasPlayer player:getRoom()){
            player.resetTurn();  //下注金额置为0
        }
    }
    /*
    检查是否仅仅剩一个人
     */
    public boolean checkEnd(){
        int count=0;
        for(texasPlayer player:getRoom()){
            if(!player.isFolded()) count++; //没有弃牌的玩家数目
            if(count>1) return false;
            //if(player.getMoney()==0) showHand_num++;  //showhand的玩家数目
        }
        //if(count==showHand_num) return true;
        return true;  //只有一个玩家，游戏结束
    }
    private void resetPlayersAction(List<texasPlayer> players) {
        for (texasPlayer player : players) {
            if (!player.isFolded()) {
                player.resetAction();
            }
        }
    }

    private boolean allPlayersActed(List<texasPlayer> players, int currentHighestBet) {
        for (texasPlayer player : players) {
            if (!player.isFolded() && (!player.hasActed() || (player.getCurrentBet() < currentHighestBet&&player.getMoney()!=0))) {
                return false;
            }
        }
        return true;
    }
    private class Deck {
        private List<String> cards = new ArrayList<>();

        public Deck() {
            // 初始化52张牌
            for (String suit : new String[]{"hearts", "diamonds", "clubs", "spades"}) {
                for (int i = 1; i <= 13; i++) {
                    cards.add(suit + "-" + i);
                }
            }
        }
        public void shuffle() {
            Collections.shuffle(cards);
        }
        public String dealCard() {
            return cards.remove(0);
        }

    }
    public Map<String, Object> dealCards(int playerCount) {
        Deck deck = new Deck();
        deck.shuffle();

        Map<String, Object> result = new HashMap<>();
        List<List<String>> playersHands = new ArrayList<>();
        List<String> boardCards = new ArrayList<>();

        // 分发每个玩家的手牌
        for (int i = 0; i < playerCount; i++) {
            List<String> hand = new ArrayList<>();
            hand.add(deck.dealCard());
            hand.add(deck.dealCard());
            playersHands.add(hand);
        }

        // 分发桌面上的五张公共牌
        for (int i = 0; i < 5; i++) {
            boardCards.add(deck.dealCard());
        }

        result.put("playersHands", playersHands);
        result.put("boardCards", boardCards);

        return result;
    }
}
