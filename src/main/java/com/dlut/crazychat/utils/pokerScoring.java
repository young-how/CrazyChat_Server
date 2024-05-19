package com.dlut.crazychat.utils;

import com.dlut.crazychat.pojo.pokerStatics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @projectName: CrazyChat_Service
 * @package: com.dlut.crazychat.utils
 * @className: pokerScoring
 * @author: younghow
 * @description: TODO
 * @date: 2024/5/19 9:59
 * @version: 1.0
 */
@Component
public class pokerScoring {
    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;
    private final String key_pokerStatics="pokerStatics";
    private final String rankList_winNum="rankList_winNum";
    private final String rankList_rankMoney="rankList_Money";
    private final String rankList_luckValue="rankList_luckValue";
    public pokerStatics getPokerStaticsByID(String id){
        pokerStatics re= (pokerStatics)redisTemplate.opsForHash().get(key_pokerStatics,id);
        if(re==null){
            re=new pokerStatics();
            re.setUserId(id);
            redisTemplate.opsForHash().put(key_pokerStatics,id,re);
        }
        re=setPokerStatics(re); //设置最新值
        return re;
    }
    public pokerStatics setPokerStatics(pokerStatics re){
        redisTemplate.opsForHash().put(key_pokerStatics,re.getUserId(),re);  //存入
        redisTemplate.opsForZSet().add(rankList_rankMoney,re.getUserId(),re.getGainMoney());  //更新积分排行榜
        redisTemplate.opsForZSet().add(rankList_winNum,re.getUserId(),re.getWinNum());
        redisTemplate.opsForZSet().add(rankList_luckValue,re.getUserId(),re.getLuckyValue());
        re.setRankLucky(getRank_luckValue(re.getUserId()));
        re.setRankWinNum(getRank_winNum(re.getUserId()));
        re.setRankMoney(getRank_Money(re.getUserId()));
        redisTemplate.opsForHash().put(key_pokerStatics,re.getUserId(),re);  //更新统计hash表
        return re;
    }
    /*
     * @param id:
      * @return long
     * @author younghow
     * @description 获取赢取积分的排行榜排名
     * @date younghow younghow
     */
    public long getRank_Money(String id){
        return redisTemplate.opsForZSet().reverseRank(rankList_rankMoney,id)+1;
    }
    /*
     * @param id:
      * @return long
     * @author younghow
     * @description 获取胜场的排行榜排名
     * @date younghow younghow
     */
    public long getRank_winNum(String id){
        return redisTemplate.opsForZSet().reverseRank(rankList_winNum,id)+1;
    }
    /*
     * @param id:
      * @return long
     * @author younghow
     * @description 获取积分排行榜排名
     * @date younghow younghow
     */
    public long getRank_luckValue(String id){
        return redisTemplate.opsForZSet().reverseRank(rankList_luckValue,id)+1;
    }
    /*
     * @param re:
      * @return long
     * @author younghow
     * @description 根据牌型计算幸运值
     * @date younghow younghow
     */
    public double CalLucyValue(pokerStatics re){
        double sum=0;
        sum+=(re.getHighCardNum()*(1.0/(0.001+0.501177)));  //高牌得分
        sum+=(re.getPairNum()*(1.0/(0.001+0.422569)));  //对子得分
        sum+=(re.getTwoPairNum()*(1.0/(0.001+0.047539)));  //两对得分
        sum+=(re.getThreeOfKindNum()*(1.0/(0.001+0.021128)));  //三条得分
        sum+=(re.getStraightNum()*(1.0/(0.001+0.003925)));  //顺子得分
        sum+=(re.getFlushNum()*(1.0/(0.001+0.00197)));  //同花得分
        sum+=(re.getFullHouseNum()*(1.0/(0.001+0.001441)));  //葫芦得分
        sum+=(re.getFourOfKindNum()*(1.0/(0.001+0.00024)));  //四条得分
        sum+=(re.getStraightFlushNum()*(1.0/(0.001+0.0000139)));  //同花顺得分
        return sum;
    }
    public pokerStatics addPokerNum(String id,boolean isWin,int winMoney,String rank){
        pokerStatics re=getPokerStaticsByID(id);
        re.setGameNum(re.getGameNum()+1);
        if(isWin){
            re.setWinNum(re.getWinNum()+1);
        }
        re.setGainMoney(re.getGainMoney()+winMoney);
        re.setWinRate(re.getWinNum()*1.0/re.getGameNum());  //计算胜率
        if(rank.equals(PokerUtils.HandRank.HIGH_CARD.toString())){
            re.setHighCardNum(re.getHighCardNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.ONE_PAIR.toString())){
            re.setPairNum(re.getPairNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.TWO_PAIR.toString())){
            re.setTwoPairNum(re.getTwoPairNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.THREE_OF_A_KIND.toString())){
            re.setThreeOfKindNum(re.getThreeOfKindNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.STRAIGHT.toString())){
            re.setStraightNum(re.getStraightNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.FULL_HOUSE.toString())){
            re.setFullHouseNum(re.getFullHouseNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.FOUR_OF_A_KIND.toString())){
            re.setFourOfKindNum(re.getFourOfKindNum()+1);
        }else if(rank.equals(PokerUtils.HandRank.STRAIGHT_FLUSH.toString())){
            re.setStraightFlushNum(re.getStraightFlushNum()+1);
        }
        re.setLuckyValue(CalLucyValue(re));  //计算幸运值
        re=setPokerStatics(re);  //保存统计信息，并更新排行榜
        return re;
    }

}
