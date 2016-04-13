package gamelogic;

/**
 * Created by kristianrosland on 13.04.2016.
 */
public enum AIType { MCTS_AI, SIMPLE_AI, MIXED;

    /**
     * @return A string representation of this AI type, as it is shown in the choice buttons in the GUI
     */
    public String toString() {
        switch (this) {
            case MCTS_AI:
                return "Advanced AI";
            case SIMPLE_AI:
                return "Simple AI";
            case MIXED:
                return "Mixed AIs";
            default: throw new IllegalStateException();
        }
    }

    /**
     * @return The AIType corresponding to the input string, as shown in the choice buttons in the GUI
     */
    public static AIType fromString(String string) {
        switch (string) {
            case "Advanced AI":
                return MCTS_AI;
            case "Simple AI":
                return SIMPLE_AI;
            case "Mixed AIs":
                return MIXED;
            default: throw new IllegalArgumentException();
        }
    }
}