package gamelogic.ai;

import gamelogic.Decision;

import java.util.*;

/**
 * Created by morten on 04.04.16.
 */
public class PokerMCTS {

    public final int playerId;
    public final int amountOfPlayers;
    public final AbstractNode rootNode;
    private final GameState initialGameState;
    private int totalSearches;

    public PokerMCTS(GameState gameState, int amountOfPlayers, int playerId) {
        this.amountOfPlayers = amountOfPlayers;
        this.playerId = playerId;
        int amountOfMoves = gameState.allDecisions().get().size();
        this.rootNode = new AINode(amountOfMoves);
        this.initialGameState = gameState;
    }

    public Decision calculateFor(long milliseconds) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + milliseconds) {
            for (int i = 0; i < 100; i++) {
                rootNode.select(totalSearches, initialGameState);
                totalSearches++;
            }
        }
        List<GameState.GameStateChange> allDecisions = initialGameState.allDecisions().get();

        AbstractNode bestNode = rootNode.children.get(0).get();
        Decision bestDecision = ((GameState.PlayerDecision)allDecisions.get(0)).decision;
        for (int i = 0; i < allDecisions.size(); i++) {
            if (rootNode.children.get(i).get().values[playerId] > bestNode.values[playerId]) {
                bestNode = rootNode.children.get(i).get();
                bestDecision = ((GameState.PlayerDecision)allDecisions.get(i)).decision;
                System.out.println(((GameState.PlayerDecision)allDecisions.get(i)).decision + ": " + rootNode.children.get(i).get().values[playerId]);
            }
        }

        return bestDecision;
    }

    public abstract class AbstractNode {
        public final double[] values; // The probability of winning for each player
        public final List<Optional<AbstractNode>> children;
        protected int searches = 0;
        protected int numberOfExploredChildren = 0;

        public AbstractNode(int numberOfMoves) {
            values = new double[amountOfPlayers];
            children = new ArrayList<>(numberOfMoves);
            for (int i = 0; i < numberOfMoves; i++){
                children.add(Optional.empty());
            }
            assert numberOfMoves == children.size();
        }

        public abstract double[] select(double totalSearches, GameState gameState);

        public double[] expand(double totalSearches, GameState gameState) {
            int childIndex = (int)(Math.random()*children.size());
            while (children.get(childIndex).isPresent()) {
                childIndex = (int)(Math.random()*children.size());
            }

            List<GameState.GameStateChange> allMoves = gameState.allDecisions().get();

            GameState newGameState = new GameState(gameState);
            Optional<ArrayList<GameState.GameStateChange>> allMovesForChild = newGameState.allDecisions();
            AbstractNode childNode;

            //TODO: Currently computes all moves for this node AND child node, which is wasteful
            if (allMovesForChild.isPresent()) {
                if (allMovesForChild.get().get(0) instanceof GameState.CardDealtToTable
                        || allMovesForChild.get().get(0) instanceof GameState.CardDealtToPlayer) {
                    childNode = new RandomNode(allMovesForChild.get().size());
                }
                else if (newGameState.currentPlayer.id == playerId) {
                    childNode = new AINode(allMovesForChild.get().size());
                }
                else {
                    childNode = new OpponentNode(allMovesForChild.get().size(), newGameState.currentPlayer.id);
                }
            }
            else {
                childNode = new TerminalNode(0);
            }
            children.set(childIndex, Optional.of(childNode));

            numberOfExploredChildren++;

            return children.get(childIndex).get().simulate(totalSearches, newGameState);
            // Not necessary to add value because expand() already does this
        }

        public double[] simulate(double totalSearches, GameState gameState) {
            assert gameState.allDecisions().isPresent() : "Tried to simulate a " + this.getClass().getSimpleName() + " when it was actually a terminal node";

            List<GameState.GameStateChange> allMoves = gameState.allDecisions().get();

            assert this.getNodeType() == allMoves.get(0).getStartingNodeType() : "NodeType of this node was " + this.getNodeType() + " but first decision is " + gameState.allDecisions().get().get(0);
            assert children.size() == allMoves.size()
                    : this.getClass().getSimpleName() + " before a " + gameState.getNextNodeType()
                    + " has " + children.size() + " children, but " + allMoves.size() + " decisisons available";



            int randomChildIndex = (int)(Math.random() * children.size());

            GameState newGameState = new GameState(gameState);
            newGameState.makeGameStateChange(allMoves.get(randomChildIndex));

            Optional<ArrayList<GameState.GameStateChange>> allChildMoves = newGameState.allDecisions();
            int numberOfMovesForChild = allChildMoves.isPresent() ? allChildMoves.get().size() : 0;

            AbstractNode childNode;
            /*
            assert gameState.getNextNodeType() == GameState.NodeType.Terminal ||
                    newGameState.getNextNodeType() == GameState.NodeType.Terminal ||
                    gameState.getNextNodeType() == newGameState.allDecisions().get().get(0).getStartingNodeType()
                    : "Next node is " + gameState.getNextNodeType() + ", the " + newGameState.allDecisions().get().size() + " moves for the node are of type " + newGameState.allDecisions().get().get(0).getStartingNodeType() ;
            */
            assert allMoves.stream().allMatch(d -> d.getStartingNodeType() == allMoves.get(0).getStartingNodeType());
            switch (newGameState.getNextNodeType()) {
                case DealCard:
                    childNode = new RandomNode(numberOfMovesForChild);
                    break;
                case PlayerDecision:
                    assert allChildMoves.isPresent() : "Tried to create a player node in a game state where there are no legal moves";
                    if (newGameState.currentPlayer.id == playerId) {
                        childNode = new AINode(numberOfMovesForChild);
                    }
                    else {
                        childNode = new OpponentNode(numberOfMovesForChild, newGameState.currentPlayer.id);
                    }
                    break;
                case Terminal:
                    childNode = new TerminalNode(numberOfMovesForChild);
                    break;
                default: throw new IllegalStateException();
            }
            return childNode.simulate(totalSearches, newGameState);
        }

        public GameState.NodeType getNodeType() {
            if (this instanceof PlayerNode) {
                return GameState.NodeType.PlayerDecision;
            }
            else if (this instanceof TerminalNode) {
                return GameState.NodeType.Terminal;
            }
            else {
                return GameState.NodeType.DealCard;
            }
        }
    }

    // Node where the AI plays.
    public abstract class PlayerNode extends AbstractNode {

        public final int playerToMove;
        public PlayerNode(int numberOfMoves, int playerToMove) {
            super(numberOfMoves);
            this.playerToMove = playerToMove;
        }

        @Override
        public double[] select(double totalSearches, GameState gameState) {

            GameState newGameState = new GameState(gameState);
            if (numberOfExploredChildren == children.size()) {
                if (children.get(0).get() instanceof PlayerNode) {
                    PlayerNode bestNode;

                    Comparator<PlayerNode> explorationValueComparator = (node1, node2) -> Double.compare(node1.explorationValue(totalSearches), node2.explorationValue(totalSearches));

                    bestNode = children.stream().map(child -> (PlayerNode)child.get())
                            .max(explorationValueComparator).get();

                    double[] eval = bestNode.select(totalSearches, newGameState);
                    addValues(values, eval);
                    return eval;
                }
                else {
                    AbstractNode randomNode = children.get((int) (Math.random() * children.size())).get();
                    double[] eval = randomNode.select(totalSearches, newGameState);
                    addValues(values, eval);
                    return eval;
                }
            }
            else {
                double[] eval = expand(totalSearches, newGameState);
                addValues(values, eval);
                return eval;
            }
        }

        public double explorationValue(double totalSearches) {
            if (searches > 0) {
                return values[playerToMove] / searches + Math.sqrt(2) * Math.sqrt(Math.log(totalSearches) / searches);
            }
            else {
                return Double.POSITIVE_INFINITY;
            }
        }
    }

    public class OpponentNode extends PlayerNode {
        public OpponentNode(int numberOfMoves, int player_to_move) {
            super(numberOfMoves, player_to_move);
        }
    }

    public class AINode extends PlayerNode {
        public AINode(int numberOfMoves) {
            super(numberOfMoves, playerId);
        }
    }

    public class TerminalNode extends AbstractNode {

        public TerminalNode(int numberOfMoves) {
            super(numberOfMoves);
            assert numberOfMoves == 0;
        }

        @Override
        public double[] select(double totalSearches, GameState gameState) {
            return terminalEval(gameState);
        }

        @Override
        public double[] expand(double totalSearches, GameState gameState) {
            return terminalEval(gameState);
        }

        @Override
        public double[] simulate(double totalSearches, GameState gameState) {
            return terminalEval(gameState);
        }
    }

    // A random node does not currently minimax. L
    public class RandomNode extends AbstractNode {

        public RandomNode(int numberOfMoves) {
            super(numberOfMoves);
        }

        @Override
        public double[] select(double totalSearches, GameState gameState) {
            GameState newGameState = new GameState(gameState);
            if (numberOfExploredChildren == children.size()) {
                AbstractNode randomNode = children.get((int) (Math.random() * children.size())).get();
                double[] eval = randomNode.select(totalSearches, newGameState);
                addValues(values, eval);
                return eval;
            }
            else {
                double[] eval = expand(totalSearches, newGameState);
                addValues(values, eval);
                return eval;
            }
        }
    }

    public static double[] terminalEval(GameState gameState) {
        //System.out.println("Getting terminal evaluation");
        double[] eval = new double[gameState.amountOfPlayers];
        for (int i = 0; i < gameState.amountOfPlayers; i++) {
            eval[i] = 0.0;
        }
        return eval; //TODO: Write properly
    }

    // Adds each newEval to oldEvals, mutating oldEvals
    public static void addValues(double[] oldEvals, double[] evalsToAdd) {
        assert oldEvals.length == evalsToAdd.length : "Tried to add evals of length " + oldEvals.length + " to evals of length " + evalsToAdd.length;
        for (int i = 0; i < oldEvals.length; i++) {
            oldEvals[i] += evalsToAdd[i];
        }
    }
    public static enum GameDecision {
        FOLD, CHECK, CALL, RAISE_MIN, RAISE_HALF_POT, RAISE_POT, ALL_IN,
    }
}
