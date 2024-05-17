package com.dlut.crazychat;

import com.dlut.crazychat.utils.PokerUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PokerUtilsTest {
    @Test
    public void testCompareHands() {
        //测试高牌vs对子
        List<String> hand1 = Arrays.asList("hearts-10", "diamonds-11", "clubs-1", "spades-3", "hearts-5");
        List<String> hand2 = Arrays.asList("hearts-10", "diamonds-10", "clubs-12", "spades-13", "hearts-1");
        int result = PokerUtils.compareHands(hand1, hand2);
        System.out.println(result);
        //对子vs两队
        hand1 = Arrays.asList("hearts-10", "diamonds-10", "clubs-12", "spades-13", "hearts-1");
        hand2 = Arrays.asList("hearts-10", "diamonds-10", "clubs-12", "spades-12", "hearts-1");
        result = PokerUtils.compareHands(hand1, hand2);
        System.out.println(result);
        //三条vs四条
        hand1 = Arrays.asList("hearts-10", "diamonds-10", "clubs-10", "spades-13", "hearts-1");
        hand2 = Arrays.asList("hearts-10", "diamonds-10", "clubs-10", "spades-10", "hearts-1");
        result = PokerUtils.compareHands(hand1, hand2);
        System.out.println(result);
        //四条vs同花
        hand1 = Arrays.asList("hearts-10", "diamonds-10", "clubs-10", "spades-10", "hearts-1");
        hand2 = Arrays.asList("clubs-2", "clubs-9", "clubs-4", "clubs-5", "clubs-6");
        result = PokerUtils.compareHands(hand1, hand2);
        System.out.println(result);
        //同花vs顺子
        hand1 = Arrays.asList("clubs-2", "clubs-9", "clubs-4", "clubs-5", "clubs-6");
        hand2 = Arrays.asList("hearts-3", "diamonds-4", "clubs-5", "spades-6", "hearts-7");
        result = PokerUtils.compareHands(hand1, hand2);
        System.out.println(result);
    }
    @Test
    public void testEvaluateBestHand() {
        //同花
        List<String> hand = Arrays.asList("hearts-2", "hearts-3");
        List<String> board = Arrays.asList("hearts-4", "hearts-5", "hearts-6", "clubs-7", "diamonds-8");
        PokerUtils.Hand bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
        //同花顺
        hand = Arrays.asList("hearts-10", "hearts-11");
        board = Arrays.asList("hearts-12", "hearts-13", "hearts-1", "clubs-7", "hearts-9");
        bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
        //对子
        hand = Arrays.asList("hearts-10", "hearts-11");
        board = Arrays.asList("clubs-10", "hearts-13", "clubs-1", "diamonds-7", "spades-9");
        bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
        //高牌
        hand = Arrays.asList("hearts-10", "hearts-11");
        board = Arrays.asList("clubs-3", "hearts-13", "clubs-1", "diamonds-7", "spades-9");
        bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
        //三队
        hand = Arrays.asList("hearts-11", "hearts-11");
        board = Arrays.asList("diamonds-11", "hearts-11", "hearts-11", "clubs-11", "spades-11");
        bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
        //葫芦
        hand = Arrays.asList("spades-10", "diamonds-10");
        board = Arrays.asList("hearts-10", "clubs-1", "hearts-1", "clubs-7", "hearts-9");
        bestHand = PokerUtils.evaluateBestHand(hand, board);
        System.out.println(bestHand.rank);
    }
}
