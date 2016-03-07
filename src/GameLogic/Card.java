package GameLogic;

import java.util.Optional;

/**
 * Class to represent poker cards. The class does not have a public constructor, rather,
 * you use of(rank, suit) to get the cards. This way, creating a card doesn't involve an allocation
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

    // Private constructor that should only be called during the class' initialization
    private Card(int rank, Suit suit) {
        assert rank >= 2 && rank <= 14;
        this.rank = rank;
        this.suit = suit;
    }

    /*
     *  Gets a card object from a rank and a suit.
     *  Two calls with the same arguments will always return the same object
    */
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

