package GameLogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public abstract class Player extends User{

    private int stackSize;
    private Table table;
    private Hand hand;

    public Player(String name, int stackSize, Table table) {
        super(name);
        this.stackSize = stackSize;
        this.table = table;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public void setHand(Card card1, Card card2) {
        this.hand = new Hand(card1, card2, table.getCommunityCards());
    }

    public abstract Decision getDecision();
}

