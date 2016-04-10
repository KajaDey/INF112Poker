package gamelogic.ai;

import gamelogic.Decision;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by morten on 04.04.16.
 */
public class GameStateTest {
    /*@Test
    public void testThatCloneReturnsEqualObjects() {
        GameState gameState = new GameState(3, Arrays.asList(0, 1, 2),
                Arrays.asList(5000L, 5000L, 5000L), 25, 50);
        gameState.makeGameStateChange(gameState.allDecisions().get().get(1));
        assertEquals(gameState, new GameState(gameState));
        gameState.makeGameStateChange(gameState.allDecisions().get().get(1));
        assertEquals(gameState, new GameState(gameState));
        gameState.makeGameStateChange(gameState.allDecisions().get().get(1));
        assertEquals(gameState, new GameState(gameState));
    }

    @Test
    public void doSomeRandomMoves() {
        for (int i = 0; i < 1000; i++) {
            GameState gameState = new GameState(4, Arrays.asList(0, 1, 2, 3),
                    Arrays.asList(5000L, 5000L, 5000L, 5000L), 25, 50);
            for (int j = 0; j < 15; j++) {
                Optional<ArrayList<GameState.GameStateChange>> decision = gameState.allDecisions();
                if (decision.isPresent()) {
                    assertTrue("allDecisions() returned 0 decisions", decision.get().size() > 0);
                    gameState.makeGameStateChange(decision.get().get((int) (Math.random() * decision.get().size())));
                }
            }
            assertEquals(gameState, new GameState(gameState));
        }
    }*/
}
