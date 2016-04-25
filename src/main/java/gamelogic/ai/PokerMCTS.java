package gamelogic.ai;

import gamelogic.*;
import gamelogic.ai.SimpleAI.AIDecision;
import java.util.*;

/**
 * Class to represent a single instance of a monte carlo tree search.
 */
public class PokerMCTS {

    public final double contemptFactor;
    public final int playerId;
    public final int playerPosition;
    public final int amountOfPlayers;
    private final AbstractNode rootNode;
    private final GameState initialGameState;

    private int totalSearches;
    private int terminalNodesSelected;

    public PokerMCTS(GameState gameState, int amountOfPlayers, int playerId, double contemptFactor) {
        this.amountOfPlayers = amountOfPlayers;
        this.playerId = playerId;
        this.contemptFactor = contemptFactor;
        this.playerPosition = gameState.players.stream()
                .filter(player -> player.id == playerId)
                .findFirst()
                .get().position;
        this.initialGameState = gameState;
        assert initialGameState.getNextNodeType() == GameState.NodeType.PLAYER_DECISION : "MCTSAI was asked to make a decision when next node is " + initialGameState.getNextNodeType();
        assert initialGameState.currentPlayer.id == playerId : "MCTSAI was asked to make a decision when player to move is " + initialGameState.currentPlayer;
        int amountOfMoves = initialGameState.allDecisions().get().size();
        this.rootNode = new AINode(amountOfMoves);
        assert amountOfMoves < 10 && rootNode.children.size() < 10 : "Root node has " + amountOfMoves + " moves: " + initialGameState.allDecisions().get();
    }

    public Decision calculateFor(long milliseconds) {
        assert initialGameState.currentPlayer.id == playerId : "Started calculating when currentPlayer is " + initialGameState.currentPlayer + ", but AI is " + initialGameState.players.get(playerPosition);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + milliseconds) {
            for (int i = 0; i < 100; i++) {
                rootNode.select(totalSearches, initialGameState, new Random(), false);
                totalSearches++;
                if (totalSearches % 100000 == 0) {
                    printProgressReport();
                }
            }
        }

        List<GameState.GameStateChange> allDecisions = initialGameState.allDecisions().get();

        double[] values = new double[allDecisions.size()];
        for (int i = 0; i < values.length; i++) {
            double value = rootNode.children.get(i).get().values[playerPosition];
            int searches = rootNode.children.get(i).get().searches;
            values[i] = value / (double)searches;

            AIDecision decision = ((GameState.AIMove)allDecisions.get(i)).decision;
            //System.out.print("Value of " + decision + " was " + values[i] + ", is ");
            switch (decision) {
                case FOLD:
                    values[i] *= 1.0 / Math.pow(contemptFactor - 0.03, 0.5);
                    break;
                case CHECK:
                    values[i] *= 1.0 / Math.pow(contemptFactor - 0.03, 0.5);
                    break;
                case RAISE_HALF_POT:
                    long betSize = initialGameState.getCurrentPot() / 2 + initialGameState.currentPlayer.currentBet;
                    values[i] *= Math.pow(contemptFactor - 0.03, (double)betSize / initialGameState.currentPlayer.stackSize);
                    break;
                case RAISE_MINIMUM:
                    betSize = initialGameState.currentPlayer.minimumRaise + initialGameState.currentPlayer.currentBet;
                    values[i] *= Math.pow(contemptFactor - 0.03, (double)betSize / initialGameState.currentPlayer.stackSize);
                    break;
                case RAISE_POT:
                    betSize = initialGameState.getCurrentPot() + initialGameState.currentPlayer.currentBet;
                    values[i] *= Math.pow(contemptFactor - 0.03, (double)betSize / initialGameState.currentPlayer.stackSize);
                    break;
                case CALL:
                    values[i] *= Math.pow(contemptFactor - 0.03, (double)initialGameState.currentPlayer.currentBet / initialGameState.currentPlayer.stackSize);
                    break;
            }
        }
        System.out.println("Values after contempt factor modification: " + Arrays.toString(values));

        double bestValue = 0.0;
        AIDecision bestDecision = AIDecision.FOLD;

        assert allDecisions.size() > 1 : "Only had " + allDecisions.size() + " decisions to make: " + allDecisions;
        for (int i = 0; i < allDecisions.size(); i++) {
            //double value = rootNode.children.get(i).get().values[playerPosition];
            //int searches = rootNode.children.get(i).get().searches;
            if (values[i] > bestValue) {
                bestValue = values[i];
                bestDecision = ((GameState.AIMove)allDecisions.get(i)).decision;
            }
        }
        printProgressReport();
        return bestDecision.toRealDecision(initialGameState.currentPlayer.currentBet, initialGameState.currentPlayer.minimumRaise,
                initialGameState.currentPlayer.stackSize, initialGameState.getCurrentPot(), initialGameState.getPlayersLeftInHand()== 1,
                initialGameState.currentPlayer.currentBet > 0 || initialGameState.communityCards.size() == 0);
    }

    public void printProgressReport() {
        System.out.println(totalSearches + " searches so far, " + terminalNodesSelected + " terminal nodes selected, size of tree: " + rootNode.sizeOfTree() + "; cards: " + initialGameState.players.get(playerPosition).holeCards);

        List<GameState.GameStateChange> allDecisions = initialGameState.allDecisions().get();

        assert playerPosition == initialGameState.currentPlayer.position;


        for (int i = 0; i < allDecisions.size(); i++) {
            double value = rootNode.children.get(i).get().values[playerPosition];
            int searches = rootNode.children.get(i).get().searches;
            System.out.printf("%-25s: %.2f%% (%.1f/%d)", allDecisions.get(i), 100.0 * value / searches, value, searches);
            System.out.println();
        }
        System.out.println();
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

        /**
         * Creates a node without initializing its children. Should only be used when simulating
         */
        private AbstractNode() {
            values = new double[amountOfPlayers];
            children = new ArrayList<>(0);
        }

        public int sizeOfTree() {
            return children.stream().reduce(0, (acc, child) -> child.isPresent() ? acc + 1 + child.get().sizeOfTree() : acc, Integer::sum);
        }

        public abstract double[] select(int totalSearches, final GameState gameState, Random random, boolean hasPassedDecisionNode);

        public double[] expand(int totalSearches, final GameState gameState, Random random, boolean hasPassedDecisionNode) {
            int childIndex = random.nextInt(children.size());
            while (children.get(childIndex).isPresent()) {
                childIndex = (childIndex + 1) % children.size();
            }

            List<GameState.GameStateChange> allMoves = gameState.allDecisions().get();
            GameState newGameState = new GameState(gameState);

            try {
                newGameState.makeGameStateChange(allMoves.get(childIndex));
            } catch (IllegalDecisionException e) {
                e.printStackTrace();
                System.exit(1);
            }

            GameState.NodeType childNodeType = newGameState.getNextNodeType();
            Optional<List<GameState.GameStateChange>> allMovesForChild = newGameState.allDecisions();

            AbstractNode childNode;

            //TODO: Currently computes all moves for this node AND child node, which may be wasteful

            switch (childNodeType) {
                case DEAL_HAND_CARD:
                case DEAL_COMMUNITY_CARD:
                    childNode = new RandomNode(allMovesForChild.get().size());
                    break;
                case PLAYER_DECISION:
                    if (newGameState.currentPlayer.id == playerId) {
                        childNode = new AINode(allMovesForChild.get().size());
                    }
                    else {
                        childNode = new OpponentNode(allMovesForChild.get().size());
                    }
                    break;
                case TERMINAL:
                    childNode = new TerminalNode(0, newGameState, totalSearches);
                    break;
                default: throw new IllegalStateException();
            }

            children.set(childIndex, Optional.of(childNode));

            //System.out.println("Creating new " + childNode.getClass().getSimpleName() + " from " + getClass().getSimpleName() + ", currentPlayer = " + gameState.currentPlayer + ", new currentPlayer: " + newGameState.currentPlayer);
            numberOfExploredChildren++;

            return children.get(childIndex).get().simulate(totalSearches, newGameState, random, hasPassedDecisionNode);

            // Not necessary to add value because expand() already does this
        }

        public double[] simulate(int totalSearches, final GameState gameState, Random random, boolean hasPassedDecisionNode) {
            assert gameState.getNextNodeType() != GameState.NodeType.TERMINAL : "Tried to simulate a " + this.getClass().getSimpleName() + " when it was actually a terminal node";

            GameState.GameStateChange randomMove = gameState.getRandomDecision(random).get();

            try {
                gameState.makeGameStateChange(randomMove);
            } catch (IllegalDecisionException e) {
                e.printStackTrace();
                System.exit(1);
            }

            AbstractNode childNode;

            switch (gameState.getNextNodeType()) {
                case DEAL_HAND_CARD:
                case DEAL_COMMUNITY_CARD:
                    childNode = new RandomNode();
                    break;
                case PLAYER_DECISION:
                    if (gameState.currentPlayer.id == playerId) {
                        childNode = new AINode();
                    }
                    else {
                        childNode = new OpponentNode();
                    }
                    break;
                case TERMINAL:
                    childNode = new TerminalNode(0, gameState, totalSearches);
                    break;
                default: throw new IllegalStateException();
            }

            return childNode.simulate(totalSearches, gameState, random, hasPassedDecisionNode);
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
    }

    /**
     * Represents a node where a player makes a decision
     */
    private abstract class PlayerNode extends AbstractNode {

        public PlayerNode(int numberOfMoves) {
            super(numberOfMoves);
        }
        /**
         * Creates a node without initializing its children. Should only be used when simulating
         */
        private PlayerNode() {
        }

        @Override
        public double[] select(int totalSearches, final GameState gameState, Random random, boolean hasPassedDecisionNode) {
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
                    }
                }
                try {
                    newGameState.makeGameStateChange(gameState.allDecisions().get().get(childIndex));
                } catch (IllegalDecisionException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                evals = bestNode.select(totalSearches, newGameState, random, hasPassedDecisionNode);

            }
            else {
                evals = expand(totalSearches, gameState, random, hasPassedDecisionNode);
                // Setting critical values is done in node expansion
            }

            addValues(values, evals);
            return evals;
        }
    }

    private class OpponentNode extends PlayerNode {
        public OpponentNode(int numberOfMoves) {
            super(numberOfMoves);
        }
        private OpponentNode() {}
    }

    private class AINode extends PlayerNode {
        public AINode(int numberOfMoves) {
            super(numberOfMoves);
        }
        private AINode() {}
    }

    /**
     * A tree node where there are no more moves to play, i.e., a showdown or a situation where everyone folded
     */
    private class TerminalNode extends AbstractNode {

        double[] terminalEval;

        public TerminalNode(int numberOfMoves, GameState gameState, int totalSearches) {
            super(numberOfMoves);
            assert numberOfMoves == 0;
            //assert gameState.getPlayersAllIn() + gameState.getPlayersLeftInHand() == 1 || gameState.getPlayersGivenHoleCards() == gameState.amountOfPlayers : gameState.getPlayersGivenHoleCards() + " players given holecards but " + gameState.amountOfPlayers + " players left in hand.";
            terminalEval = terminalEval(gameState, totalSearches);
            addValues(values, terminalEval);
        }

        @Override
        public double[] select(int totalSearches, GameState gameState, Random random, boolean hasPassedDecicionNode) {
            terminalNodesSelected++;
            this.searches++;
            addValues(values, terminalEval);
            return terminalEval;
        }

        @Override
        public double[] expand(int totalSearches, GameState gameState, Random random, boolean hasPassedDecicionNode) {
            throw new IllegalStateException("Terminal nodes should never be expanded");
        }

        @Override
        public double[] simulate(int totalSearches, GameState gameState, Random random, boolean hasPassedDecicionNode) {
            this.searches++;
            return terminalEval;
        }
    }

    /**
     * A node where something random happens. Either a community card being dealed, or a hole card dealt to another player
     */
    public class RandomNode extends AbstractNode {

        public RandomNode(int numberOfMoves) {
            super(numberOfMoves);
            //assert numberOfMoves > 0 : "Tried to create a " + this.getClass().getSimpleName() + " with " + numberOfMoves + " children.";
        }

        /**
         * Creates a node without initializing its children. Should only be used when simulating
         */
        public RandomNode() {
        }

        @Override
        public double[] select(int totalSearches, final GameState gameState, Random random, boolean hasPassedDecicionNode) {
            searches++;
            assert gameState.allDecisions().isPresent() && gameState.allDecisions().get().size() == children.size() : "Tried to select node with wrong gamestate, " + gameState.allDecisions().orElse(new ArrayList<>(0)).size() + " decisions but " + children.size() + " children.";
            assert children.size() > 0 : "Tried to select a " + this.getClass().getSimpleName() + " with " + children.size() + " children, " + gameState.getPlayersAllIn() + " players all in, " + gameState.getPlayersLeftInHand() + " players left in hand, players: " + gameState.players;
            if (numberOfExploredChildren == children.size()) {

                int randomIndex = random.nextInt(children.size());
                GameState newGameState = new GameState(gameState);
                List<GameState.GameStateChange> moves = newGameState.allDecisions().get();

                try {
                    newGameState.makeGameStateChange(moves.get(randomIndex));
                } catch (IllegalDecisionException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                double[] eval = children.get(randomIndex).get().select(totalSearches, newGameState, random, hasPassedDecicionNode);
                addValues(values, eval);
                return eval;
            }
            else {
                double[] eval = expand(totalSearches, gameState, random, hasPassedDecicionNode);
                addValues(values, eval);
                return eval;
            }
        }
    }

    /**
     * Returns the evaluation of a terminal node (Showdown, or everyone folded)
     * @param gameState
     * @param totalSearches
     * @return
     */
    private static double[] terminalEval(GameState gameState, int totalSearches) {

        class PlayerAndScore implements Comparable<PlayerAndScore> {
            public final Player player;
            public final int handScore;

            PlayerAndScore(Player player, int handScore) {
                this.player = player;
                this.handScore = handScore;
            }
            @Override
            public int compareTo(PlayerAndScore other) {
                return Integer.compare(this.handScore, other.handScore);
            }
        }

        if (gameState.getPlayersLeftInHand() + gameState.getPlayersAllIn() == 1) {
            long pot = 0;
            for (Player player : gameState.players) {
                pot += player.contributedToPot;
                player.contributedToPot = 0;
            }
            for (Player player : gameState.players) {
                if (player.isInHand || player.isAllIn) {
                    player.stackSize += pot;
                }
            }
            assert gameState.sumOfChipsInPlay(gameState.players) == gameState.allChipsOnTable : gameState.sumOfChipsInPlay(gameState.players) + " chips in play, but started with " + gameState.allChipsOnTable;
            double[] eval = new double[gameState.amountOfPlayers];
            for (int i = 0; i < gameState.players.size(); i++) {
                assert gameState.players.get(i).position == i;
                eval[i] = (double)gameState.players.get(i).stackSize / (double)gameState.allChipsOnTable;
                assert eval[i] > 0 : "Everyone folded, but player " + gameState.players.get(i) + " has eval " + eval[i];
            }
            return eval;
        }
        assert gameState.communityCards.size() == 5 : "Getting terminal eval with " + gameState.getPlayersLeftInHand() + " players left in hands and " + gameState.getPlayersAllIn() + " players all in.";

        GameState newGameState = new GameState(gameState);
        double[] eval = new double[newGameState.amountOfPlayers];
        Pot pot = new Pot();
        for (Player player : newGameState.players) {
            if (player.holeCards.size() < 2) {
                newGameState.giveHoleCards(player.id);
            }
            assert (!player.isInHand && !player.isAllIn) || player.holeCards.size() == 2 : "Tried to get terminal eval after " + totalSearches + " searches, but " + player + (player.isAllIn ? " (is all in)" : "") + " has " + player.holeCards.size() + " holecards. " + gameState.getPlayersGivenHoleCards() + " players were given hole cards, players: " + newGameState.players;
            assert player.contributedToPot >= 0 : player + " tried to contribute " + player.contributedToPot + " to pot.";
            pot.addToPot(player.id, player.contributedToPot);
            player.contributedToPot = 0;
        }

        assert newGameState.sumOfChipsInPlay(newGameState.players) + pot.getPotSize() == newGameState.allChipsOnTable;

        ArrayList<PlayerAndScore> fasterHandsList = new ArrayList<>();
        for (Player player : newGameState.players) {
            if (player.isInHand) {
                fasterHandsList.add(new PlayerAndScore(player, new HandCalculator(new Hand(player.holeCards.get(0), player.holeCards.get(1), newGameState.communityCards)).getHandScore()));
            }
        }
        fasterHandsList.sort(PlayerAndScore::compareTo);

        Collections.reverse(fasterHandsList);
        for (PlayerAndScore pair : fasterHandsList) {
            long share = pot.getSharePlayerCanWin(pair.player.id);
            pair.player.stackSize += share;
        }

        for (Player player : newGameState.players) {
            player.stackSize += pot.getSharePlayerCanWin(player.id);
        }

        assert pot.getPotSize() == 0 : "Still " + pot.getPotSize() + " chips left in pot";

        for (int i = 0; i < newGameState.players.size(); i++) {
            assert newGameState.players.get(i).position == i;
            eval[i] = (double)newGameState.players.get(i).stackSize / (double)newGameState.allChipsOnTable;
        }

        assert newGameState.sumOfChipsInPlay(newGameState.players) == newGameState.allChipsOnTable :
                "Sum of player chips is " + GameState.sumOfChipsInPlay(newGameState.players) + ", but started with " + newGameState.allChipsOnTable + " on table.";

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
