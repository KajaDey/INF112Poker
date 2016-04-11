package gamelogic.ai;

import gamelogic.Decision;
import gamelogic.Hand;
import gamelogic.Pot;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to represent a single instance of a monte carlo tree search.
 */
public class PokerMCTS {

    public final int playerId;
    public final int playerPosition;
    public final int amountOfPlayers;
    private final AbstractNode rootNode;
    private final GameState initialGameState;

    private Optional<ArrayList<NodeEval>> criticalEvals = Optional.empty(); // Evaluation and amount of searches done for each possible move at the critical node.
    private int totalSearches;
    private int terminalNodesSelected;

    public PokerMCTS(GameState gameState, int amountOfPlayers, int playerId) {
        this.amountOfPlayers = amountOfPlayers;
        this.playerId = playerId;
        this.playerPosition = gameState.players.stream()
                .filter(player -> player.id == playerId)
                .findFirst()
                .get().position;
        this.initialGameState = gameState;
        int amountOfMoves = initialGameState.allDecisions().get().size();
        this.rootNode = new RandomNode(amountOfMoves);
    }

    public Decision calculateFor(long milliseconds) {
        assert initialGameState.currentPlayer.id == playerId : "Started calculating when currentPlayer is " + initialGameState.currentPlayer + ", but AI is " + initialGameState.players.get(playerPosition);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + milliseconds) {
            for (int i = 0; i < 100; i++) {
                rootNode.select(totalSearches, initialGameState, false);
                totalSearches++;
                if (totalSearches % 50000 == 0) {
                    assert rootNode.children.size() <= 50 && rootNode.children.size() >= 45  : "Rootnode for MCTS had " + rootNode.children.size() + " children.";
                    assert rootNode.children.get(0).get().children.size() <= 49 && rootNode.children.get(0).get().children.size() >= 44 : "2nd level node has " + rootNode.children.get(0).get().children.size() + " children, should have 44-49.";
                    assert rootNode.children.get(0).get().searches > 10;
                    assert rootNode.children.get(0).get().children.get(0).isPresent();
                    printProgressReport();
                }
            }
        }
        GameState gameState = new GameState(initialGameState);
        for (Player player : gameState.players) {
            if (player.holeCards.size() == 0) {
                gameState.giveHoleCards(player.id);
            }
        }
        List<GameState.GameStateChange> allDecisions = gameState.allDecisions().get();

        assert criticalEvals.get().size() == allDecisions.size() : "Has values for " + criticalEvals.get().size() + " moves, but " + allDecisions.size() + " moves (" + allDecisions + ")";
        double bestValue = 0.0;

        double[] values = new double[criticalEvals.get().size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = criticalEvals.get().get(i).eval / criticalEvals.get().get(i).searches;
        }

        Decision bestDecision = new Decision(Decision.Move.FOLD);
        assert allDecisions.size() > 1;
        for (int i = 0; i < allDecisions.size(); i++) {
            if (values[i] > bestValue) {
                bestValue = values[i];
                bestDecision = ((GameState.PlayerDecision)allDecisions.get(i)).decision;
            }
        }
        printProgressReport();
        return bestDecision;
    }

    public void printProgressReport() {
        System.out.println(totalSearches + " searches so far, " + terminalNodesSelected + " terminal nodes selected, size of tree: " + rootNode.sizeOfTree() + "; cards: " + initialGameState.players.get(playerPosition).holeCards);
        GameState gameState = new GameState(initialGameState);
        for (Player player : gameState.players) {
            if (player.holeCards.size() == 0) {
                gameState.giveHoleCards(player.id);
            }
        }
        List<GameState.GameStateChange> allDecisions = gameState.allDecisions().get();

        for (int i = 0; i < criticalEvals.get().size(); i++) {
            GameState newGameState = new GameState(gameState);
            newGameState.makeGameStateChange(allDecisions.get(i));
            System.out.printf("%-25s: %.2f%%, %s", allDecisions.get(i), 100 * criticalEvals.get().get(i).eval / criticalEvals.get().get(i).searches, criticalEvals.get().get(i));
            System.out.println();
            //System.out.println("\t" + newGameState.allDecisions().map(decisions -> decisions.stream().map(Object::toString).reduce("", (str1, str2) -> str1 + str2 + "\n\t")).orElse("No moves"));
        }
        System.out.println();
    }

    private static class NodeEval {
        public double eval;
        public int searches;
        NodeEval(double v1, int v2) {
            this.eval = v1;
            this.searches = v2;
        }
        public String toString() {
            return "(" + eval + "/" + searches + ")";
        }
    }

    /**
     * Represent a node in the monte carlo search tree. The tree only stores its value (winning probability) for each player,
     * not the gamestate. The children are stored in a list, where children[0] represents the gamestate
     * reached by making the 0th move from gamestate.allDecisions()
     */
    private abstract class AbstractNode {
        public final double[] values; // The probability of winning for each player
        public final List<Optional<AbstractNode>> children;
        public int searches = 0;
        protected int numberOfExploredChildren = 0;

        public AbstractNode(int numberOfMoves) {
            values = new double[amountOfPlayers];
            children = new ArrayList<>(numberOfMoves);
            for (int i = 0; i < numberOfMoves; i++){
                children.add(Optional.empty());
            }
            assert numberOfMoves == children.size();
        }

        public int sizeOfTree() {
            return children.stream().reduce(0, (acc, child) -> child.isPresent() ? acc + 1 + child.get().sizeOfTree() : acc, Integer::sum);
        }

        public abstract double[] select(int totalSearches, final GameState gameState, boolean hasPassedDecisionNode);

        public double[] expand(int totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
            int childIndex = (int)(Math.random()*children.size());
            while (children.get(childIndex).isPresent()) {
                childIndex = (int)(Math.random()*children.size());
            }

            List<GameState.GameStateChange> allMoves = gameState.allDecisions().get();

            GameState newGameState = new GameState(gameState);
            newGameState.makeGameStateChange(allMoves.get(childIndex));

            GameState.NodeType childNodeType = newGameState.getNextNodeType();
            Optional<ArrayList<GameState.GameStateChange>> allMovesForChild = newGameState.allDecisions();

            AbstractNode childNode;

            //TODO: Currently computes all moves for this node AND child node, which may be wasteful

            switch (childNodeType) {
                case DealCard:
                    childNode = new RandomNode(allMovesForChild.get().size());
                    break;
                case PlayerDecision:
                    if (newGameState.currentPlayer.id == playerId) {
                        childNode = new AINode(allMovesForChild.get().size());
                    }
                    else {
                        childNode = new OpponentNode(allMovesForChild.get().size(), newGameState.currentPlayer.position);
                    }
                    break;
                case Terminal:
                    childNode = new TerminalNode(0, newGameState, totalSearches);
                    break;
                default: throw new IllegalStateException();
            }

            children.set(childIndex, Optional.of(childNode));

            //System.out.println("Creating new " + childNode.getClass().getSimpleName() + " from " + getClass().getSimpleName() + ", currentPlayer = " + gameState.currentPlayer + ", new currentPlayer: " + newGameState.currentPlayer);
            numberOfExploredChildren++;

            if (!hasPassedDecisionNode && this instanceof AINode) {

                double[] evals = childNode.simulate(totalSearches, newGameState, true);
                if (!criticalEvals.isPresent()) {
                    criticalEvals = Optional.of(new ArrayList<>());
                    for (int i = 0; i < allMoves.size(); i++) {
                        criticalEvals.get().add(new NodeEval(0.0, 0));
                    }
                }
                criticalEvals.get().get(childIndex).eval
                        += evals[playerPosition];
                criticalEvals.get().get(childIndex).searches += 1;
                return evals;
            }
            else {
                return children.get(childIndex).get().simulate(totalSearches, newGameState, hasPassedDecisionNode);
            }
            // Not necessary to add value because expand() already does this
        }

        public double[] simulate(int totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
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
                    assert numberOfMovesForChild < 20 : "Tried to create player node with " + numberOfMovesForChild + " moves available";
                    if (newGameState.currentPlayer.id == playerId) {
                        childNode = new AINode(numberOfMovesForChild);
                    }
                    else {
                        childNode = new OpponentNode(numberOfMovesForChild, newGameState.currentPlayer.position);
                    }
                    break;
                case Terminal:
                    childNode = new TerminalNode(numberOfMovesForChild, newGameState, totalSearches);
                    break;
                default: throw new IllegalStateException();
            }
            //System.out.println("Creating new " + childNode.getClass().getSimpleName() + " from " + this.getClass().getSimpleName() + ", currentPlayer = " + gameState.currentPlayer + ", new currentPlayer: " + newGameState.currentPlayer);

            assert hasPassedDecisionNode || !(this instanceof OpponentNode) : "Found opponent node for " + gameState.currentPlayer + " without passing decision node (AI is " + gameState.players.get(playerPosition) + ") after " + totalSearches + " searches.";
            if (!hasPassedDecisionNode && this instanceof AINode) {
                double[] evals = childNode.simulate(totalSearches, newGameState, true);
                //System.out.println(this.getClass().getSimpleName() + " at search #" + totalSearches + ", has stacksize " + gameState.players.get(playerPosition).stackSize + ", currentBet " + gameState.players.get(playerPosition).currentBet + " and moves " + allMoves.stream().map(Object::toString).reduce(String::concat).get());
                if (!criticalEvals.isPresent()) {
                    criticalEvals = Optional.of(new ArrayList<>());
                    for (int i = 0; i < allMoves.size(); i++) {
                        criticalEvals.get().add(new NodeEval(0.0, 0));
                    }
                }
                assert this.children.size() == criticalEvals.get().size()
                        : "Node has children " + children.stream().map(Object::toString).reduce("", String::concat)
                        + " but crit evals are " + criticalEvals.get().stream().map(Object::toString).reduce("", String::concat)
                        + " and moves are " + allMoves.stream().map(Object::toString).reduce("", String::concat);
                criticalEvals.get().get(randomChildIndex).eval += evals[playerPosition];
                criticalEvals.get().get(randomChildIndex).searches += 1;
                // This is a throwaway node, so not neccessary to store the values and number of searches
                return evals;
            }
            return childNode.simulate(totalSearches, newGameState, hasPassedDecisionNode);
        }

        /*
        * Returns the exploration value for a node. Nodes that have been explored little will get higher values
        * @param totalSearches Total amount of searches done from rootnode
        * @param playerPosition Position of the player making the move
         */
        public double explorationValue(int totalSearches, int playerPosition) {
            if (searches > 0) {
                return values[playerPosition] / searches + Math.sqrt(2) * Math.sqrt(Math.log(totalSearches) / searches);
            }
            else {
                return Double.POSITIVE_INFINITY;
            }
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

    /**
     * Represents a node where a player makes a decision
     */
    private abstract class PlayerNode extends AbstractNode {

        public final int positionToMove;
        public PlayerNode(int numberOfMoves, int positionToMove) {
            super(numberOfMoves);
            this.positionToMove = positionToMove;
        }

        @Override
        public double[] select(int totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
            searches++;
            double[] evals;
            GameState newGameState = new GameState(gameState);
            //System.out.println("Selecting " + getClass().getSimpleName() + " with " + numberOfExploredChildren + " children explored, " + hasPassedDecisionNode);
            if (numberOfExploredChildren == children.size()) {
                //System.out.println(getClass().getSimpleName() + " has children: " + children.stream().map(Optional::get).map(Object::getClass).map(Class::getSimpleName).reduce(String::concat));
                AbstractNode bestNode = children.get(0).get();
                double bestExplorationValue = bestNode.explorationValue(totalSearches, gameState.currentPlayer.position);
                int childIndex = 0;
                for (int i = 1; i < children.size(); i++) {
                    double explorationValue = children.get(i).get().explorationValue(totalSearches, gameState.currentPlayer.position);
                    if (explorationValue > bestExplorationValue) {
                        bestNode = children.get(i).get();
                        bestExplorationValue = explorationValue;
                        childIndex = i;
                        //System.out.println("Chose new best " + bestNode.getClass().getSimpleName() + " with i " +i + " and exploration value " + explorationValue);
                    }
                }
                assert bestNode != this;
                newGameState.makeGameStateChange(gameState.allDecisions().get().get(childIndex));

                if (!hasPassedDecisionNode && this instanceof AINode) {
                    evals = bestNode.select(totalSearches, newGameState, true);
                    if (!criticalEvals.isPresent()) {
                        criticalEvals = Optional.of(new ArrayList<>());
                        for (int i = 0; i < children.size(); i++) {
                            criticalEvals.get().add(new NodeEval(0.0, 0));
                        }
                    }
                    criticalEvals.get().get(childIndex).eval += evals[playerPosition];
                    criticalEvals.get().get(childIndex).searches += 1;
                } else {
                    evals = bestNode.select(totalSearches, newGameState, hasPassedDecisionNode);
                }
            }
            else {
                evals = expand(totalSearches, gameState, hasPassedDecisionNode);
                // Setting critical values is done in node expansion
            }

            addValues(values, evals);
            return evals;
        }
    }

    private class OpponentNode extends PlayerNode {
        public OpponentNode(int numberOfMoves, int player_to_move) {
            super(numberOfMoves, player_to_move);
        }
    }

    private class AINode extends PlayerNode {
        public AINode(int numberOfMoves) {
            super(numberOfMoves, playerId);
        }
    }

    /**
     * A tree node where there are no more moves to play, i.e., a showdown or a situation where everyone folded
     */
    private class TerminalNode extends AbstractNode {

        public TerminalNode(int numberOfMoves, GameState gameState, int totalSearches) {
            super(numberOfMoves);
            assert numberOfMoves == 0;
            addValues(values, terminalEval(gameState, totalSearches));
        }

        @Override
        public double[] select(int totalSearches, GameState gameState, boolean hasPassedDecicionNode) {
            terminalNodesSelected++;
            this.searches++;
            return terminalEval(gameState, totalSearches);
        }

        @Override
        public double[] expand(int totalSearches, GameState gameState, boolean hasPassedDecicionNode) {
            throw new IllegalStateException("Terminal nodes should never be expanded");
        }

        @Override
        public double[] simulate(int totalSearches, GameState gameState, boolean hasPassedDecicionNode) {
            //assert values[0] == terminalEval(gameState, totalSearches)[0] : "Terminal node has values " + Arrays.toString(values) + " but values were now computed to " + Arrays.toString(terminalEval(gameState, totalSearches));
            this.searches++;
            return terminalEval(gameState, totalSearches);
        }
    }

    /**
     * A node where something random happens. Either a community card being dealed, or a hole card dealt to another player
     */
    public class RandomNode extends AbstractNode {

        public RandomNode(int numberOfMoves) {
            super(numberOfMoves);
        }

        @Override
        public double[] select(int totalSearches, final GameState gameState, boolean hasPassedDecicionNode) {
            searches++;
            if (numberOfExploredChildren == children.size()) {

                int randomIndex = (int) (Math.random() * children.size());
                GameState newGameState = new GameState(gameState);
                List<GameState.GameStateChange> moves = newGameState.allDecisions().get();

                newGameState.makeGameStateChange(moves.get(randomIndex));

                double[] eval = children.get(randomIndex).get().select(totalSearches, newGameState, hasPassedDecicionNode);
                addValues(values, eval);
                return eval;
            }
            else {
                double[] eval = expand(totalSearches, gameState, hasPassedDecicionNode);
                addValues(values, eval);
                return eval;
            }
        }
    }

    public static double[] terminalEval(GameState gameState, int totalSearches) {

        class Pair<T, U> {
            final T v1;
            final U v2;
            Pair(T v1, U v2) {
                this.v1 = v1;
                this.v2 = v2;
            }
        }
        GameState newGameState = new GameState(gameState);
        double[] eval = new double[newGameState.amountOfPlayers];
        Pot pot = new Pot();
        for (Player player : newGameState.players) {
            assert player.contributedToPot >= 0 : player + " tried to contribute " + player.contributedToPot + " to pot.";
            pot.addToPot(player.id, player.contributedToPot);
            assert player.holeCards.size() == 2 : "Tried to get terminal eval after " + totalSearches + " searches, but player " + player.id + " has " + player.holeCards.size() + " holecards.";
        }

        long playerChipsSum = newGameState.players.stream().reduce(0L, (acc, player) -> acc + player.stackSize, Long::sum);
        assert playerChipsSum + pot.getPotSize() == newGameState.allChipsOnTable;

        ArrayList<Player> handsList = newGameState.players.stream()
                .filter(p -> p.isInHand)
                .map(p -> new Pair(p, new Hand(p.holeCards.get(0), p.holeCards.get(1), newGameState.communityCards)))
                .sorted((pair1, pair2) -> ((Hand)pair1.v2).compareTo(((Hand)pair2.v2)))
                .map(pair -> (Player)pair.v1)
                .collect(Collectors.toCollection(ArrayList<Player>::new));

        Collections.reverse(handsList);
        for (Player player : handsList) {
            player.stackSize += pot.getSharePlayerCanWin(player.id);
        }

        for (Player player : newGameState.players) {
            player.contributedToPot = 0;
        }

        newGameState.players.stream()
                .filter(p -> !handsList.contains(p))
                .forEach(p -> {
                    p.stackSize += pot.getSharePlayerCanWin(p.id);
                    p.contributedToPot = 0;
                });

        assert pot.getPotSize() == 0 : "Still " + pot.getPotSize() + " chips left in pot";

        for (int i = 0; i < newGameState.players.size(); i++) {
            assert newGameState.players.get(i).position == i;
            eval[i] = (double)newGameState.players.get(i).stackSize / (double)newGameState.allChipsOnTable;
        }

        playerChipsSum = newGameState.players.stream().reduce(0L, (acc, player) -> acc + player.stackSize + player.contributedToPot, Long::sum);
        assert playerChipsSum == newGameState.allChipsOnTable :
                "Sum of player chips is " + playerChipsSum + ", but started with " + newGameState.allChipsOnTable + " on table.";

        assert Arrays.stream(eval).reduce(0.0, Double::sum) > 0.999 && Arrays.stream(eval).reduce(0.0, Double::sum) < 1.001
                : "Error: winning probs is " + Arrays.toString(eval) + " (sum=" + Arrays.stream(eval).reduce(0.0, Double::sum) + ")";

        return eval;
    }

    // Adds each newEval to oldEvals, mutating oldEvals
    public static void addValues(double[] oldEvals, double[] evalsToAdd) {
        assert oldEvals.length == evalsToAdd.length : "Tried to add evals of length " + oldEvals.length + " to evals of length " + evalsToAdd.length;
        for (int i = 0; i < oldEvals.length; i++) {
            oldEvals[i] += evalsToAdd[i];
        }
    }
}
