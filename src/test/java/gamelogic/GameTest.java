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
 * Created by Ragnhild Aalvik on 03/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, GameController.class, GUIMain.class})

public class GameTest {
    private GameController gameControllerMock, gameControllerSpy;
    private Game gameSpy;
    static final Logger logger = new Logger("Test-game", "");

    /**
     * Creates spy-objects of GameController and Game. Removes delays.
     * @param aiType Type of AI to use
     * @param numPlayers number of AIs to add
     * @throws Exception
     */
    private void setupGameWithAIs(int BB, int SB, AIType aiType, int numPlayers) throws Exception {
        GameSettings gameSettings = spy(new GameSettings(5000, BB, SB, numPlayers, 10, aiType, 30));
        when(gameSettings.valid()).thenReturn(true);

        gameControllerSpy = spy(new GameController(gameSettings, logger));
        gameSpy = spy(new Game(gameSettings, gameControllerSpy, logger));

        // Replaces getAIDecision-method in GameController to avoid unnecessary delay
        doAnswer((Answer<Decision>) arg ->
                ((GameClient)arg.getArguments()[0]).getDecision(100L))
                .when(gameControllerSpy, "getAIDecision", any(GameClient.class));

        // Removes delays
        doNothing().when(gameControllerSpy).delay(anyLong());
        doNothing().when(gameSpy, "delay", anyLong());

        // Replaces any new Game-object with gameSpy
        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class), any(Logger.class)).thenReturn(gameSpy);
    }

    @Test
    public void testGameTwoPlayersBothAllIn() throws Exception {
        gameControllerMock = mock(GameController.class);
        PowerMockito.doReturn(new Decision(Decision.Move.ALL_IN)).when(gameControllerMock).getDecisionFromClient(anyInt());

        gameSpy = spy(new Game(new GameSettings(5000, 50, 25, 2, 10, AIType.MCTS_AI, 30), gameControllerMock, logger));
        doNothing().when(gameSpy, "delay", anyLong());

        gameSpy.addPlayer("Ragnhild", 0);
        gameSpy.addPlayer("Kristian", 1);

        gameSpy.playGame();
    }

    @Test
    public void testPlayGameWithSixSimpleAIs() throws Exception {
        setupGameWithAIs(500, 250, AIType.SIMPLE_AI, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }
    @Test
    public void testPlayGameWithSixMCTSAIs() throws Exception {
        setupGameWithAIs(500, 250, AIType.MCTS_AI, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }

    @Test
    public void testPlayGameWithSixMixedAIs() throws Exception {
        setupGameWithAIs(500, 250, AIType.MIXED, 6);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }

    @Test
    public void testPlayGameWithTwoMCTSAIsIllegalSizedBlinds() throws Exception {

        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            setupGameWithAIs(2005, 700, AIType.MCTS_AI, 2);

            Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
            gameThread.join();
        }
    }
}