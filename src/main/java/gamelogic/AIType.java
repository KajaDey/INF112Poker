package gamelogic;

/**
 * Created by Kristian Rosland on 13.04.2016.
 * Enum to represent the different kinds of AI
 * Mixed means a 50% chance of either
 */
public enum AIType { MCTS_AI, SIMPLE_AI, MIXED;

    /**
     * @return A string representation of this AI type, as it is shown in the choice buttons in the GUI
     */
    public String toString() {
        switch (this) {
            case MCTS_AI:
                return "Advanced";
            case SIMPLE_AI:
                return "Simple";
            case MIXED:
                return "Mixed";
            default: throw new IllegalStateException();
        }
    }

    /**
     * @return The AIType corresponding to the input string, as shown in the choice buttons in the GUI
     */
    public static AIType fromString(String string) {
        switch (string) {
            case "Advanced":
                return MCTS_AI;
            case "Simple":
                return SIMPLE_AI;
            case "Mixed":
                return MIXED;
            default: throw new IllegalArgumentException();
        }
    }
}