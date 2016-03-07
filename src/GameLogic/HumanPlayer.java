package GameLogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name, int stackSize) {
        super(name, stackSize);
    }

    @Override
    public Decision getDecision() {
        return null;
    }
}
