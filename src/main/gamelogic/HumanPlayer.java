package main.gamelogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name, int stackSize, Table table) {
        super(name, stackSize, table);
    }

    @Override
    public Decision getDecision() {
        return null;
    }
}
