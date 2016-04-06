package gamelogic.ai;

import gamelogic.Card;
import gamelogic.Decision;
import gamelogic.Hand;
import gamelogic.Pot;

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
    private int totalSearches;

    public PokerMCTS(GameState gameState, int amountOfPlayers, int playerId, List<Card> holeCards) {
        this.amountOfPlayers = amountOfPlayers;
        this.playerId = playerId;
        this.playerPosition = gameState.players.stream()
                .filter(player -> player.id == playerId)
                .findFirst()
                .get().position;
        int amountOfMoves = gameState.allDecisions().get().size();
        this.rootNode = new AINode(amountOfMoves);
        this.initialGameState = gameState;
        initialGameState.players.get(playerId).holeCards.addAll(holeCards);
    }

    public Decision calculateFor(long milliseconds) {
        System.out.println("Starting MCTS with " + amountOfPlayers + " for player " + playerId);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + milliseconds) {
            for (int i = 0; i < 1; i++) {
                rootNode.select(totalSearches, initialGameState);
                totalSearches++;
            }
        }
        System.out.println("Did " + totalSearches + " searches");
        List<GameState.GameStateChange> allDecisions = initialGameState.allDecisions().get();

        AbstractNode bestNode = rootNode.children.get(0).get();
        Decision bestDecision = new Decision(Decision.Move.FOLD);
        assert rootNode.children.size() == allDecisions.size();
        assert allDecisions.size() > 4;
        for (int i = 0; i < allDecisions.size(); i++) {
            if (rootNode.children.get(i).get().values[playerPosition] > bestNode.values[playerPosition]) {
                bestNode = rootNode.children.get(i).get();
                bestDecision = ((GameState.PlayerDecision)allDecisions.get(i)).decision;
            }
            System.out.println(((GameState.PlayerDecision)allDecisions.get(i)).decision + ": " + rootNode.children.get(i).get().values[playerPosition]);
        }

        return bestDecision;
    }

    public static double[] arraySum (double[] array1, double[] array2) {
        assert array1.length == array2.length;
        double[] result = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            result[i] = array1[i] + array2[i];
        }
        return result;
    }

    public static double[] findAIEvals(AbstractNode node, int amountOfPlayers) {
        if (node instanceof AINode) {
            return node.values;
        }
        else if (node instanceof TerminalNode) {
            return node.values;
        }
        else {
            return node.children
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(child -> findAIEvals(child, amountOfPlayers))
                    .reduce(new double[amountOfPlayers], PokerMCTS::arraySum);
        }
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
                    childNode = new OpponentNode(allMovesForChild.get().size(), newGameState.currentPlayer.position);
                }
            }
            else {
                childNode = new TerminalNode(0, newGameState);
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
                        childNode = new OpponentNode(numberOfMovesForChild, newGameState.currentPlayer.position);
                    }
                    break;
                case Terminal:
                    childNode = new TerminalNode(numberOfMovesForChild, newGameState);
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

        public final int positionToMove;
        public PlayerNode(int numberOfMoves, int positionToMove) {
            super(numberOfMoves);
            this.positionToMove = positionToMove;
        }

        @Override
        public double[] select(double totalSearches, GameState gameState) {


            if (numberOfExploredChildren == children.size()) {
                GameState newGameState = new GameState(gameState);
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
                double[] eval = expand(totalSearches, gameState);
                addValues(values, eval);
                return eval;
            }
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
        public double[] select(double totalSearches, GameState gameState) {

            return terminalEval(gameState);
        }

        @Override
        public double[] expand(double totalSearches, GameState gameState) {
            return terminalEval(gameState);
        }

        @Override
        public double[] simulate(double totalSearches, GameState gameState) {
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
        public double[] select(double totalSearches, GameState gameState) {

            if (numberOfExploredChildren == children.size()) {
                GameState newGameState = new GameState(gameState);
                AbstractNode randomNode = children.get((int) (Math.random() * children.size())).get();
                double[] eval = randomNode.select(totalSearches, newGameState);
                addValues(values, eval);
                return eval;
            }
            else {
                double[] eval = expand(totalSearches, gameState);
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
