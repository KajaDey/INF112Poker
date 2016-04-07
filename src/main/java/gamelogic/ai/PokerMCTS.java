package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.Hand;
import gamelogic.Pot;
import gui.GUIMain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by morten on 04.04.16.
 */
public class PokerMCTS {

    public final int playerId;
    public final int playerPosition;
    public final int amountOfPlayers;
    public final AbstractNode rootNode;
    private final GameState initialGameState;

    private Optional<ArrayList<NodeEval>> criticalEvals = Optional.empty(); // Evaluation and amount of searches done for each possible move at the critical node.
    private int totalSearches;

    public PokerMCTS(GameState gameState, int amountOfPlayers, int playerId, List<Card> holeCards) {
        this.amountOfPlayers = amountOfPlayers;
        this.playerId = playerId;
        this.playerPosition = gameState.players.stream()
                .filter(player -> player.id == playerId)
                .findFirst()
                .get().position;
        this.initialGameState = gameState;
        initialGameState.players.get(playerPosition).holeCards.addAll(holeCards);
        initialGameState.deck.removeAll(holeCards);
        int amountOfMoves = initialGameState.allDecisions().get().size();
        this.rootNode = new RandomNode(amountOfMoves);
    }

    public Decision calculateFor(long milliseconds) {
        System.out.println("Starting MCTS with " + amountOfPlayers + " for player " + playerId);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + milliseconds) {
            for (int i = 0; i < 100; i++) {
                rootNode.select(totalSearches, initialGameState, false);
                totalSearches++;
                if (totalSearches % 20000 == 0) {
                    //assert rootNode.children.stream().map(Optional::get).map(child -> child.searches).reduce(0, Integer::sum) == totalSearches :
                    //"Searches for children: " + rootNode.children.stream().map(Optional::get).map(child -> child.searches).reduce(0, Integer::sum) + ", total searches: " + totalSearches;
                    assert rootNode.children.size() == 50;
                    assert rootNode.children.get(0).get().children.size() == 49 : "2nd level node has " + rootNode.children.get(0).get().children.size() + " children, should have 49.";
                    assert rootNode.children.get(0).get().searches > 10;
                    assert rootNode.children.get(0).get().children.get(0).get().searches >= 2;
                    printProgressReport();
                }
            }
        }
        System.out.println("Did " + totalSearches + " searches, size of tree: " + rootNode.sizeOfTree());

        GameState gameState = new GameState(initialGameState);
        for (Player player : gameState.players) {
            while (player.holeCards.size() < 2) {
                player.holeCards.add(gameState.deck.remove(gameState.deck.size() - 1));
            }
        }

        gameState.playersGivenHolecards = amountOfPlayers;
        List<GameState.GameStateChange> allDecisions = gameState.allDecisions().get();

        //List<NodeEval> evals = findAIEvals(rootNode, amountOfPlayers, playerPosition);

        assert criticalEvals.get().size() == allDecisions.size() : "Has values for " + criticalEvals.get().size() + " moves, but " + allDecisions.size() + " moves";
        double bestValue = 0.0;

        double[] values = new double[criticalEvals.get().size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = criticalEvals.get().get(i).eval / criticalEvals.get().get(i).searches;
        }

        Decision bestDecision = new Decision(Decision.Move.FOLD);
        assert allDecisions.size() > 4;
        for (int i = 0; i < allDecisions.size(); i++) {
            if (values[i] > bestValue) {
                bestValue = values[i];
                bestDecision = ((GameState.PlayerDecision)allDecisions.get(i)).decision;
            }
            //GUIMain.debugPrintln(((GameState.PlayerDecision)allDecisions.get(i)).decision + ": " + values[i] + ", " + criticalEvals.get().get(i));
        }
        return bestDecision;
    }
    public void printProgressReport() {
        GameState gameState = new GameState(initialGameState);
        for (Player player : gameState.players) {
            while (player.holeCards.size() < 2) {
                player.holeCards.add(gameState.deck.remove(gameState.deck.size() - 1));
            }
        }
        gameState.playersGivenHolecards = gameState.amountOfPlayers;
        List<GameState.GameStateChange> allDecisions = gameState.allDecisions().get();

        for (int i = 0; i < criticalEvals.get().size(); i++) {
            System.out.printf("%-25s: %.1f%%, %s", allDecisions.get(i), 100 * criticalEvals.get().get(i).eval / criticalEvals.get().get(i).searches, criticalEvals.get().get(i));
            System.out.println();
        }
        System.out.println();
    }
    /*
    public static List<NodeEval<Double, Integer>> mergeEvals (List<NodeEval<Double, Integer>> array1, List<NodeEval<Double, Integer>> array2) {
        //assert array1.size() + array2.size() > 0;
        assert array1.size() == array2.size();
        List<NodeEval> result = new ArrayList<>();
        for (int i = 0; i < Math.max(array1.size(), array2.size()); i++) {
            if (array1.size() < i) {
                array1.add(new NodeEval<>(0.0, 0));
            }
            if (array2.size() < i) {
                array2.add(new NodeEval<>(0.0, 0));
            }
            result.add(new NodeEval(array1.get(i).v1 + array2.get(i).v1, array1.get(i).v2 + array2.get(i).v2));
        }
        return result;
    }
*/
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
    /*
    public static List<NodeEval> findAIEvals(AbstractNode node, int amountOfPlayers, int playerPosition) {
        if (node instanceof AINode) {
            System.out.println("Found AINode with evals: " + Arrays.toString(node.values));
            assert node.children.size() < 20 : "Error: AInode has " + node.children.size() + " children, which is way too many";
            List<NodeEval> values = node.children.stream()
                    .map((option) -> option.isPresent() ? new NodeEval(option.get().values[playerPosition], option.get().searches) : new NodeEval(0.0, 0))
                    .reduce(new ArrayList<>(), (acc, pair) -> { acc.add(pair); return acc; }, (list1, list2) -> {
                        list1.addAll(list2);
                        return list2; }
                        );
            return values;
        }
        else if (node instanceof TerminalNode) {
            // If opponent folds right away, the AI doesn't need to make any decisions
            System.out.println("Hit terminal node");
            return new ArrayList<>();
        }
        else {
            System.out.println("Found " + node.getClass().getSimpleName() + " with " + node.numberOfExploredChildren + " children");
            assert node.numberOfExploredChildren > 0;
            if (node.numberOfExploredChildren == 0) {
                return new ArrayList<>();
            }
            else {

                return node.children
                        .stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(child -> findAIEvals(child, amountOfPlayers, playerPosition))
                        .reduce(PokerMCTS::mergeEvals).get();
            }
        }
    }
    */

    public abstract class AbstractNode {
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

        public abstract double[] select(double totalSearches, final GameState gameState, boolean hasPassedDecisionNode);

        public double[] expand(double totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
            int childIndex = (int)(Math.random()*children.size());
            while (children.get(childIndex).isPresent()) {
                childIndex = (int)(Math.random()*children.size());
            }

            List<GameState.GameStateChange> allMoves = gameState.allDecisions().get();

            GameState newGameState = new GameState(gameState);
            newGameState.makeGameStateChange(allMoves.get(childIndex));

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
                    childNode = new OpponentNode(allMovesForChild.get().size(), newGameState.currentPlayer.position);
                }
            }
            else {
                childNode = new TerminalNode(0, newGameState);
            }
            children.set(childIndex, Optional.of(childNode));

            numberOfExploredChildren++;

            if (!hasPassedDecisionNode && this instanceof AINode) {

                double[] evals = childNode.simulate(totalSearches, newGameState, true);
                if (!criticalEvals.isPresent()) {
                    criticalEvals = Optional.of(new ArrayList<>());
                    for (int i = 0; i < allMoves.size(); i++) {
                        criticalEvals.get().add(new NodeEval(0.0, 0));
                    }
                }
                criticalEvals.get().get(childIndex).eval += evals[playerPosition];
                criticalEvals.get().get(childIndex).searches += 1;
                return evals;
            }
            else {
                return children.get(childIndex).get().simulate(totalSearches, newGameState, hasPassedDecisionNode);
            }
            // Not necessary to add value because expand() already does this
        }

        public double[] simulate(double totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
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
                    childNode = new TerminalNode(numberOfMovesForChild, newGameState);
                    break;
                default: throw new IllegalStateException();
            }
            if (!hasPassedDecisionNode && this instanceof AINode) {
                assert gameState.communityCards.isEmpty() : "AI decision was at a node with community cards " + gameState.communityCards.stream().map(Object::toString).reduce(String::concat).get();
                double[] evals = childNode.simulate(totalSearches, newGameState, true);
                //System.out.println(this.getClass().getSimpleName() + " at search #" + totalSearches + ", has stacksize " + gameState.players.get(playerPosition).stackSize + ", currentBet " + gameState.players.get(playerPosition).currentBet + " and moves " + allMoves.stream().map(Object::toString).reduce(String::concat).get());
                if (!criticalEvals.isPresent()) {
                    criticalEvals = Optional.of(new ArrayList<>());
                    for (int i = 0; i < allMoves.size(); i++) {
                        criticalEvals.get().add(new NodeEval(0.0, 0));
                    }
                }
                criticalEvals.get().get(randomChildIndex).eval += evals[playerPosition];
                criticalEvals.get().get(randomChildIndex).searches += 1;
                // This is a throwaway node, so not neccessary to store the values and number of searches
                return evals;
            }
            return childNode.simulate(totalSearches, newGameState, hasPassedDecisionNode);
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

        public final int positionToMove;
        public PlayerNode(int numberOfMoves, int positionToMove) {
            super(numberOfMoves);
            this.positionToMove = positionToMove;
        }

        @Override
        public double[] select(double totalSearches, final GameState gameState, boolean hasPassedDecisionNode) {
            searches++;
            double[] evals;
            if (numberOfExploredChildren == children.size()) {
                GameState newGameState = new GameState(gameState);
                if (children.get(0).get() instanceof PlayerNode) {

                    PlayerNode bestNode = (PlayerNode)children.get(0).get();
                    double bestExplorationValue = bestNode.explorationValue(totalSearches);
                    int childIndex = 0;
                    for (int i = 1; i < children.size(); i++) {
                        double explorationValue = ((PlayerNode)children.get(i).get()).explorationValue(totalSearches);
                        if (((PlayerNode)children.get(i).get()).explorationValue(totalSearches) > bestExplorationValue) {
                            bestNode = ((PlayerNode)children.get(i).get());
                            bestExplorationValue = explorationValue;
                            childIndex = i;
                        }
                    }
                    newGameState.makeGameStateChange(gameState.allDecisions().get().get(childIndex));

                    //Comparator<PlayerNode> explorationValueComparator = (node1, node2) -> Double.compare(node1.explorationValue(totalSearches), node2.explorationValue(totalSearches));
                    if (!hasPassedDecisionNode && bestNode instanceof AINode) {
                        evals = bestNode.select(totalSearches, newGameState, true);
                        if (!criticalEvals.isPresent()) {
                            criticalEvals = Optional.of(new ArrayList<>());
                            for (int i = 0; i < children.size(); i++) {
                                criticalEvals.get().add(new NodeEval(0.0, 0));
                            }
                        }
                        criticalEvals.get().get(children.indexOf(bestNode)).eval += evals[playerPosition];
                        criticalEvals.get().get(children.indexOf(bestNode)).searches += 1;
                    }
                    else {
                        evals = bestNode.select(totalSearches, newGameState, hasPassedDecisionNode);
                    }
                }
                else {
                    int randomChildIndex = (int) (Math.random() * children.size());
                    newGameState.makeGameStateChange(gameState.allDecisions().get().get(randomChildIndex));

                    evals = children.get(randomChildIndex).get().select(totalSearches, newGameState, hasPassedDecisionNode);
                }
            }
            else {
                evals = expand(totalSearches, gameState, hasPassedDecisionNode);
                // Setting critical values is done in node expansion
            }

            addValues(values, evals);
            return evals;
        }

        public double explorationValue(double totalSearches) {
            if (searches > 0) {
                return values[positionToMove] / searches + Math.sqrt(2) * Math.sqrt(Math.log(totalSearches) / searches);
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

        public TerminalNode(int numberOfMoves, GameState gameState) {
            super(numberOfMoves);
            assert numberOfMoves == 0;
            addValues(values, terminalEval(gameState));
        }

        @Override
        public double[] select(double totalSearches, GameState gameState, boolean hasPassedDecicionNode) {

            return terminalEval(gameState);
        }

        @Override
        public double[] expand(double totalSearches, GameState gameState, boolean hasPassedDecicionNode) {
            return terminalEval(gameState);
        }

        @Override
        public double[] simulate(double totalSearches, GameState gameState, boolean hasPassedDecicionNode) {
            assert values[0] == terminalEval(gameState)[0] : "Terminal node has values " + Arrays.toString(values) + " but values were now computed to " + Arrays.toString(terminalEval(gameState));
            return terminalEval(gameState);
        }
    }

    // A random node does not currently minimax. L
    public class RandomNode extends AbstractNode {

        public RandomNode(int numberOfMoves) {
            super(numberOfMoves);
        }

        @Override
        public double[] select(double totalSearches, final GameState gameState, boolean hasPassedDecicionNode) {
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

    public static double[] terminalEval(GameState gameState) {

        class Pair<T, U> {
            final T v1;
            final U v2;
            Pair(T v1, U v2) {
                this.v1 = v1;
                this.v2 = v2;
            }
        }

        double[] eval = new double[gameState.amountOfPlayers];
        Pot pot = new Pot();
        for (Player player : gameState.players) {
            pot.addToPot(player.id, player.contributedToPot);
            assert player.holeCards.size() == 2 : "Tried to get terminal eval, but player " + player.id + " has " + player.holeCards.size() + " holecards.";
        }

        ArrayList<Player> handsList = gameState.players.stream()
                .filter(p -> p.isInHand)
                .map(p -> new Pair(p, new Hand(p.holeCards.get(0), p.holeCards.get(1), gameState.communityCards)))
                .sorted((pair1, pair2) -> ((Hand)pair1.v2).compareTo(((Hand)pair2.v2)))
                .map(pair -> (Player)pair.v1)
                .collect(Collectors.toCollection(ArrayList<Player>::new));

        Collections.reverse(handsList);
        for (Player player : handsList) {
            player.stackSize += pot.getSharePotPlayerCanWin(player.id);
            player.contributedToPot = 0;
        }

        gameState.players.stream()
                .filter(p -> !handsList.contains(p))
                .forEach(p -> {
                    p.stackSize += pot.getSharePotPlayerCanWin(p.id);
                    p.contributedToPot = 0;
                });


        for (int i = 0; i < gameState.players.size(); i++) {
            assert gameState.players.get(i).position == i;
            eval[i] = (double)gameState.players.get(i).stackSize / (double)gameState.allChipsOnTable;
        }

        long playerChipsSum = gameState.players.stream().reduce(0L, (acc, player) -> acc + player.stackSize + player.contributedToPot, Long::sum);
        assert playerChipsSum == gameState.allChipsOnTable :
                "Sum of player chips is " + playerChipsSum + ", but started with " + gameState.allChipsOnTable + " on table.";

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
    public static enum GameDecision {
        FOLD, CHECK, CALL, RAISE_MIN, RAISE_HALF_POT, RAISE_POT, ALL_IN,
    }
}
