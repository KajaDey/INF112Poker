package gamelogic.ai;

import gamelogic.Card;

import java.util.List;

/**
 * Created by morten on 18.04.16.
 */
public class HandEstimator {
    /**
     * Gives a very rough estimate of the quality of a set of holeCards
     * @return A number between 7 and about 100
     */
    public static double handQuality(Card card1, Card card2, List<Card> communityCards) {
        double handQuality = card1.rank + card2.rank;

        if (communityCards.size() > 0 ) {
            communityCards.sort(Card::compareTo);

            // Increase hand quality based on how likely a straight is
            short rankMask = 0;
            if (card1.rank == 14 || card2.rank == 14 || communityCards.get(communityCards.size() - 1).rank == 14) {
                rankMask = 0b0100_0000_0000_0010;
            }
            int rank = 2;
            //
            communityLoop: for (Card card : communityCards) {
                while (rank < 14) {
                    if (card.rank == rank) {
                        rankMask |= 1 << rank;
                        rank++;
                        continue communityLoop;
                    }
                    else if (card1.rank == rank || card2.rank == rank) {
                        rankMask |= 1 << rank;
                    }
                    rank++;
                }
            }

            // For each set of 5 consecutive cards (i.e., 4 through 9), checks how many cards you have in that range
            for (int i = 1; i < 11; i++) {
                int mask = rankMask & (0b0000_0000_0001_1111 << i);
                mask = mask - ((mask >>> 1) & 0x55555555);
                mask = (mask & 0x33333333) + ((mask >>> 2) & 0x33333333);
                int bitsSet =  (((mask + (mask >>> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;

                switch (bitsSet) {
                    // These increments may happen several times, increasing the handQuality if there are many outs for straights
                    case 3:
                        if (communityCards.size() == 3) {
                            handQuality += 2;
                        }
                        break;
                    case 4:
                        if (communityCards.size() == 3) {
                            handQuality += 8;
                        }
                        else if (communityCards.size() == 4) {
                            handQuality += 4;
                        }
                        break;
                    case 5:
                        handQuality += 25;
                        break;
                }
            }


            // Increase hand quality for chances for flush
            int ofSameSuit;
            if (card1.suit == card2.suit) {
                ofSameSuit = 2;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofSameSuit++;
                    }
                }
            }
            else {
                int ofCard1Suit = 1;
                int ofCard2Suit = 1;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofCard1Suit++;
                    }
                    else if (card.suit == card2.suit) {
                        ofCard2Suit++;
                    }
                }
                ofSameSuit = Math.max(ofCard1Suit, ofCard2Suit);
            }
            switch (ofSameSuit) {
                case 3:
                    if (communityCards.size() == 3) {
                        handQuality += 3;
                    }
                    break;
                case 4:
                    if (communityCards.size() == 3) {
                        handQuality += 15;
                    }
                    else if (communityCards.size() == 4) {
                        handQuality += 8;
                    }
                    break;
                case 5:
                    handQuality += 30;
                    break;
            }

            // Increase hand quality for chances for flush
            if (card1.suit == card2.suit) {
                int ofSameRank = 2;
                for (Card card : communityCards) {
                    if (card.suit == card1.suit) {
                        ofSameRank++;
                    }
                }
                handQuality += xOfAKindEval(ofSameRank, communityCards.size());

            }
            else {
                int ofCard1Rank = 1;
                int ofCard2Rank = 1;
                for (Card card : communityCards) {
                    if (card.rank == card1.rank) {
                        ofCard1Rank++;
                    }
                    else if (card.rank == card2.rank) {
                        ofCard2Rank++;
                    }
                }
                handQuality += xOfAKindEval(ofCard1Rank, communityCards.size());
                handQuality += xOfAKindEval(ofCard2Rank, communityCards.size());
            }

        }
        else {
            if (card1.suit == card2.suit) {
                handQuality *= 1.2;
            }
            int rankDistance = Math.abs(card1.rank - card2.rank);
            switch (rankDistance) {
                case 0:
                    handQuality = handQuality * 1.4 + 15;
                    break;
                case 1:
                    handQuality = handQuality * 1.2 + 6;
                    break;
                case 2:
                    handQuality = handQuality * 1.1 + 3;
                    break;
                case 3:
                    handQuality = handQuality * 1.05 + 1;
                    break;
                default:
            }
        }
        return handQuality;
    }

    public static double xOfAKindEval(int ofSameRank, int holeCardsGiven) {
        switch (ofSameRank) {
            case 2:
                switch (holeCardsGiven) {
                    case 3:
                        return 18;
                    case 4:
                        return 15;
                    case 5:
                        return 12;
                }
                break;
            case 3:
                switch (holeCardsGiven) {
                    case 3:
                        return 22;
                    case 4:
                        return 20;
                    case 5:
                        return 18;
                }
                break;
            case 4:
                return 40;
        }
        return 0;
    }

    /**
     * Returns a raise decision, which becomes higher if the hand is good
     * May return a decision to raise higher than stacksize. This gets converted to an ALL_IN decision.
     * @param randomModifier Modifier that gets multipled by the handquality
     */
    public static SimpleAI.AIDecision getRaiseAmount(double randomModifier, double handQuality, double contemptFactor) {
        if (randomModifier * (handQuality / 30.0) > 1 / contemptFactor) { // If the hand is really good
            return SimpleAI.AIDecision.RAISE_POT;
        }
        else if (randomModifier * (handQuality / 24.0) > 1 / contemptFactor) { // If the hand is really good
            return SimpleAI.AIDecision.RAISE_HALF_POT;
        }
        else {
            return SimpleAI.AIDecision.RAISE_MINIMUM;
        }
    }
}
