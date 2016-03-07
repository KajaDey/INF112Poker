package GameLogic;

import java.util.Optional;

/**
 * Created by morten on 07.03.16.
 */
public class Card implements Comparable<Card> {

    private static final Card[] cards = new Card[52];

    // Initialize cards
    static {
        for (int i = 0; i < 52; i+= 4) {
            int rank = i + 2;
            cards[i] = new Card(rank, Suit.SPADES);
            cards[i + 1] = new Card(rank, Suit.HEARTS);
            cards[i + 1] = new Card(rank, Suit.DIAMONDS);
            cards[i + 1] = new Card(rank, Suit.CLUBS);
        }
    }
    public final int rank;
    public final Suit suit;

    // Private constructor that should only
    private Card(int rank, Suit suit) {
        assert rank >= 2 && rank <= 14;
        this.rank = rank;
        this.suit = suit;
    }

    public static Optional<Card> of(int rank, Suit suit) {
        if (rank >= 2 && rank <= 14) {
            return Optional.of(cards[(rank - 2) * 4 + suit.ordinal()]);
        }
        else
            return Optional.empty();
    }

    @Override
    /**
     * Compares two cards solely based on their rank, with Ace as 14,
     * i.e., ace of spades compared to ace of hearts returns 0
     */
    public int compareTo(Card other) {
        return Integer.compare(this.rank, other.rank);
    }

    public static enum Suit {
        SPADES, HEARTS, DIAMONDS, CLUBS
    }
}

