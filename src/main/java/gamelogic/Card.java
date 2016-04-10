package gamelogic;

import java.util.Optional;

/**
 * Class to represent a single poker card. The class does not have a public constructor, rather,
 * you use Card.of(rank, suit) to get the cards. This way, creating a card doesn't involve
 * an allocation, and performs better.
 */
public class Card implements Comparable<Card> {

    private static final Card[] cards = new Card[52];

    // Initialize cards when class is loaded
    static {
        for (int i = 0; i < 52; i+= 4) {
            int rank = i / 4 + 2;
            cards[i] = new Card(rank, Suit.SPADES);
            cards[i + 1] = new Card(rank, Suit.HEARTS);
            cards[i + 2] = new Card(rank, Suit.DIAMONDS);
            cards[i + 3] = new Card(rank, Suit.CLUBS);
        }
    }

    public final int rank;
    public final Suit suit;

    /**
     * Private constructor that should only be called during the class' initialization
     * Use Card.of() to get references to new cards
     *
     * @param rank the rank of a single card (int: 2-14)
     * @param suit the suit of a single card (Suit: spades, clubs, hearts, diamonds)
     */
    private Card(int rank, Suit suit) {
        assert rank >= 2 && rank <= 14;
        this.rank = rank;
        this.suit = suit;
    }

    /**
     *  Gets a card object from a rank and a suit.
     *  Two calls with the same arguments will always return the same object
     *  @param rank the rank of a single card (int: 2-14)
     *  @param suit the suit of a single card (Suit: spades, clubs, hearts, diamonds)
    */
    public static Optional<Card> of(int rank, Suit suit) {
        if (rank >= 2 && rank <= 14) {
            return Optional.of(cards[(rank - 2) * 4 + suit.ordinal()]);
        }
        else
            return Optional.empty();
    }

    /**
     * Gets an array of all 52 cards, in order
     */
    public static Card[] getAllCards() {
        return cards.clone();
    }

    @Override
    /**
     * Compares two cards solely based on their rank, with Ace as 14,
     * Two cards of the same rank are equal, i.e., ace of spades compared to ace of hearts returns 0
     *
     * @param other a Card to compare against the current card
     */
    public int compareTo(Card other) {
        return Integer.compare(this.rank, other.rank);
    }

    /**
     *
     * @return a name that the GUI can use to display cards.
     */
    public String getCardNameForGui(){

        String rank,suit;

        if(this.suit == Suit.SPADES)
            suit = "Spades ";
        else if (this.suit == Suit.HEARTS)
            suit = "Hearts ";
        else if (this.suit == Suit.DIAMONDS)
            suit = "Diamonds ";
        else
            suit = "Clubs ";

        if(this.rank == 14 || this.rank == 1)
            rank = "1";
        else
        rank = this.rank+"";

        return suit + rank;

    }

    public String getRankString(){

        String rankString = ""+rank;
        switch (rank) {
            case 14 : rankString = "Ace";
                break;
            case 13 : rankString = "King";
                break;
            case 12 : rankString = "Queen";
                break;
            case 11 : rankString = "Jack";
                break;

        }
        return rankString;
    }

    public static enum Suit {
        SPADES, HEARTS, DIAMONDS, CLUBS;

        @Override
        public String toString() {
            switch (this) {
                case SPADES: return "\u2660";
                case HEARTS: return "\u2665";
                case DIAMONDS: return "\u2666";
                case CLUBS: return "\u2663";
                default: throw new IllegalStateException();
            }
        }
    }

    @Override
    public String toString() {
        String royal = "";
        if (rank == 14) {
            royal = "A";
        }
        else if (rank == 13) {
            royal = "K";
        }
        else if (rank == 12) {
            royal = "Q";
        }
        else if (rank == 11) {
            royal = "J";
        }
        return suit.toString() + (royal.equals("") ? rank : royal);
    }
}

