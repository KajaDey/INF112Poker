package main.java.gamelogic.ai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by morten on 10.03.16.
 */
public class MonteCarloTreeNodeTest {


    /**
     * A simple two-player game on a 8x8 board, to test the MCTS implementation.
     * Each player has one king that starts at the bottom of the board,
     * and wins if they can move their king to the other side of the board
     */
    private static class GamePosition {
        boolean to_move;
        byte[] board = new byte[64]; // 0 = empty, 1 = white piece, 2 = black piece

        public GamePosition() {
            to_move = true; // True = white to move
            board[59] = 1;
            board[60] = 2;
        }

        public GamePosition do_move(Move move) {
            GamePosition newPosition = new GamePosition();
            newPosition.board = this.board.clone();

            assert newPosition.board != null;

            newPosition.board[move.toPosition] = newPosition.board[move.fromPosition];
            newPosition.board[move.fromPosition] = 0;
            newPosition.to_move = !this.to_move;
            return newPosition;
        }

        public ArrayList<Move> allLegalMoves() {
            ArrayList<Move> moves = new ArrayList<>();
            if (getTerminalEvaluation().isPresent()) {
                return moves;
            }

            int whitePos = -1;
            int blackPos = -1;
            for (int i = 0; i < 64; i++) {
                if (board[i] == 1) {
                    assert whitePos == -1;
                    whitePos = i;
                }
                if (board[i] == 2) {
                    assert blackPos == -1;
                    blackPos = i;
                }
            }
            assert whitePos > 0 && blackPos > 0;

            assert board != null;
            int piecePosition = -1;
            for (int i = 0; i < board.length; i++) {
                if (to_move && board[i] == 1) {
                    piecePosition = i;
                    break;
                }
                else if (!to_move && board[i] == 2) {
                    piecePosition = i;
                    break;
                }
            }
            assert piecePosition != -1;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    int newPosition = piecePosition + x + 8 * y;
                    if (newPosition >= 0 && newPosition < 64 && board[newPosition] == 0) {
                        moves.add(new Move(piecePosition, newPosition));
                    }
                }
            }
            return moves;
        }

        public Optional<Double> getTerminalEvaluation() {
            for (int i = 0; i < 8; i++) {
                if (board[i] == 1) {
                    return Optional.of(1.0);
                }
                else if (board[i] == 2) {
                    return Optional.of(-1.0);
                }
            }
            return Optional.empty();
        }

        public String toString() {
            return "To move: " + to_move + "\nBoard: " + Arrays.toString(board);
        }
    }

    private static class Move {
        public final int fromPosition;
        public final int toPosition;

        private Move(int fromPosition, int toPosition) {
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
        }
        public String toString() {
            return "From: " + fromPosition + ", to: " + toPosition;
        }
    }
    // @Test
    public void simpleGameTest() {
        MonteCarloTreeNode<GamePosition, Move> rootNode = new MonteCarloTreeNode<>(new GamePosition(), GamePosition::do_move, GamePosition::allLegalMoves, GamePosition::getTerminalEvaluation);
        for (int i = 0; i < 5000_000; i++) {
            rootNode.select();
            if (i % 100_000 == 0) {
                for (int j = 0; j < rootNode.children.size(); j++) {
                Optional<MonteCarloTreeNode> child = rootNode.children.get(j);
                    if (child.isPresent()) {
                        System.out.println(child.get() + ", move: " + rootNode.allLegalMoves.apply(rootNode.position).get(j));

                        for (int k = 0; k < child.get().children.size(); k++) {
                            Optional<MonteCarloTreeNode> grandChild = (Optional<MonteCarloTreeNode>)child.get().children.get(k);
                            if (grandChild.isPresent()) {
                                System.out.println("\t" + grandChild.get()
                                        + ", move: " + ((ArrayList<Move>)(child.get().allLegalMoves.apply(child.get().position))).get(k));
                                //+ ((Optional<MonteCarloTreeNode>) child.get().children.get(k)).get().position);
                            }
                            else {
                                System.out.println("\tNone");
                            }
                        }

                    }
                    else {
                        System.out.println("None");
                    }
                }
                System.out.println();
            }
        }
        for (Optional<MonteCarloTreeNode> child : rootNode.children) {
            if (child.isPresent()) {
                System.out.println(child.get());
            }
            else {
                System.out.println("None");
            }
        }
    }
}