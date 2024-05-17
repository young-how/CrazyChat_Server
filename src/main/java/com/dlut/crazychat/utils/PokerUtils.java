package com.dlut.crazychat.utils;

import java.util.*;

public class PokerUtils {

    public enum HandRank {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH
    }

    public static class Hand implements Comparable<Hand> {
        public HandRank rank;
        List<Integer> highCards;

        public Hand(HandRank rank, List<Integer> highCards) {
            this.rank = rank;
            this.highCards = highCards;
        }

        @Override
        public int compareTo(Hand other) {
            if (this.rank.ordinal() != other.rank.ordinal()) {
                return Integer.compare(this.rank.ordinal(), other.rank.ordinal());
            } else {
                for (int i = 0; i < this.highCards.size(); i++) {
                    int compare = Integer.compare(this.highCards.get(i), other.highCards.get(i));
                    if (compare != 0) {
                        return compare;
                    }
                }
                return 0;
            }
        }
    }

    public static int compareHands(List<String> hand1, List<String> hand2) {
        Hand h1 = evaluateHand(hand1);
        Hand h2 = evaluateHand(hand2);
        return h1.compareTo(h2);
    }

    public static Hand evaluateBestHand(List<String> hand, List<String> board) {
        List<String> combined = new ArrayList<>(hand);
        combined.addAll(board);

        List<Hand> allHands = new ArrayList<>();
        List<List<String>> allCombinations = generateCombinations(combined, 5);

        for (List<String> combo : allCombinations) {
            allHands.add(evaluateHand(combo));
        }

        return Collections.max(allHands);
    }

    private static List<List<String>> generateCombinations(List<String> cards, int k) {
        List<List<String>> combinations = new ArrayList<>();
        generateCombinations(cards, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private static void generateCombinations(List<String> cards, int k, int start, List<String> current, List<List<String>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinations(cards, k, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }

    private static Hand evaluateHand(List<String> hand) {
        List<Integer> values = new ArrayList<>();
        Map<Integer, Integer> valueCount = new HashMap<>();
        Map<String, Integer> suits = new HashMap<>();

        for (String card : hand) {
            String[] parts = card.split("-");
            int value = Integer.parseInt(parts[1]);
            String suit = parts[0];

            values.add(value);
            valueCount.put(value, valueCount.getOrDefault(value, 0) + 1);
            suits.put(suit, suits.getOrDefault(suit, 0) + 1);
        }

        Collections.sort(values, Collections.reverseOrder());

        boolean flush = suits.size() == 1;
        boolean straight = isStraight(values);

        if (straight && flush) {
            return new Hand(HandRank.STRAIGHT_FLUSH, values);
        }
        if (valueCount.containsValue(4)) {
            return new Hand(HandRank.FOUR_OF_A_KIND, getHighCards(valueCount, 4));
        }
        if (valueCount.containsValue(3) && valueCount.containsValue(2)) {
            return new Hand(HandRank.FULL_HOUSE, getHighCards(valueCount, 3, 2));
        }
        if (flush) {
            return new Hand(HandRank.FLUSH, values);
        }
        if (straight) {
            return new Hand(HandRank.STRAIGHT, values);
        }
        if (valueCount.containsValue(3)) {
            return new Hand(HandRank.THREE_OF_A_KIND, getHighCards(valueCount, 3));
        }
        if (Collections.frequency(new ArrayList<>(valueCount.values()), 2) == 2) {
            return new Hand(HandRank.TWO_PAIR, getHighCards(valueCount, 2, 2));
        }
        if (valueCount.containsValue(2)) {
            return new Hand(HandRank.ONE_PAIR, getHighCards(valueCount, 2));
        }
        return new Hand(HandRank.HIGH_CARD, values);
    }

    private static boolean isStraight(List<Integer> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) - values.get(i + 1) != 1) {
                return false;
            }
        }
        return true;
    }

    private static List<Integer> getHighCards(Map<Integer, Integer> valueCount, int... counts) {
        List<Integer> highCards = new ArrayList<>();
        List<Integer> singleCards = new ArrayList<>();

        for (int count : counts) {
            for (Map.Entry<Integer, Integer> entry : valueCount.entrySet()) {
                if (entry.getValue() == count) {
                    highCards.add(entry.getKey());
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : valueCount.entrySet()) {
            if (entry.getValue() == 1) {
                singleCards.add(entry.getKey());
            }
        }

        Collections.sort(highCards, Collections.reverseOrder());
        Collections.sort(singleCards, Collections.reverseOrder());

        highCards.addAll(singleCards);

        return highCards;
    }
}

