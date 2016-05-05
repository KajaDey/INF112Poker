package gamelogic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import gui.GUIMain;
import gui.GameSettings;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import java.util.ArrayList;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;


/**
 * Created by pokki on 03/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, GameController.class, GUIMain.class})

public class GameTest {
    private GameController gameControllerMock, gameControllerSpy;
    private Game gameSpy;

    /**
     * Creates spy-objects of GameController and Game
     * @param aiType Type of AI to use
     * @param numPlayers number of AIs to add
     * @throws Exception
     */
    private void setupGameWithAIs(AIType aiType, int numPlayers) throws Exception {
        GameSettings gameSettings = new GameSettings(5000, 500, 250, numPlayers, 10, aiType, 30);
        gameControllerSpy = spy(new GameController(gameSettings));
        gameSpy = spy(new Game(new GameSettings(GameSettings.DEFAULT_SETTINGS), gameControllerSpy));

        // Replaces getAIDecision-method in GameController to avoid unnecessary delay
        doAnswer(new Answer<Decision>() {
            @Override
            public Decision answer(InvocationOnMock aiClient) throws Throwable{
                return ((GameClient)aiClient.getArguments()[0]).getDecision(100L);
            }
        }).when(gameControllerSpy, "getAIDecision", any(GameClient.class));

        // Removes delays
        doNothing().when(gameControllerSpy).delay(anyLong());
        doNothing().when(gameSpy, "delay", anyLong());

        // Replaces any new Game-object with gameSpy
        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class)).thenReturn(gameSpy);
    }

    @Test
    public void testGameTwoPlayersBothAllIn() throws Exception {
        gameControllerMock = mock(GameController.class);
        PowerMockito.doReturn(new Decision(Decision.Move.ALL_IN)).when(gameControllerMock).getDecisionFromClient(anyInt());

        gameSpy = spy(new Game(new GameSettings(5000, 50, 25, 2, 10, AIType.MCTS_AI, 30), gameControllerMock));
        doNothing().when(gameSpy, "delay", anyLong());

        gameSpy.addPlayer("Ragnhild", 0);
        gameSpy.addPlayer("Kristian", 1);

        gameSpy.playGame();
    }

    @Test
    public void testPlayGameWithSixSimpleAIs() throws Exception {
        setupGameWithAIs(AIType.SIMPLE_AI, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }
    @Test
    public void testPlayGameWithSixMCTSAIs() throws Exception {
        setupGameWithAIs(AIType.MCTS_AI, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }

    @Test
    public void testPlayGameWithSixMixedAIs() throws Exception {
        setupGameWithAIs(AIType.MIXED, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }
}
