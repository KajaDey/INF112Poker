package gamelogic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import gui.GUIMain;
import gui.GameSettings;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.method;


/**
 * Created by pokki on 03/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, GameController.class, GUIMain.class})

public class GameTest {
    private GameController gameController, gameControllerSpy;
    private Game game, gameSpy;

    /**
     * Creates a mock game controller for use when testing hard coded decision
     */
    private void createGameControllerMock() {
        gameController = PowerMockito.mock(GameController.class);
        PowerMockito.doReturn(new Decision(Decision.Move.ALL_IN)).when(gameController).getDecisionFromClient(anyInt());
    }

    /**
     * Creates a game controller spy for use when there are AI players
     */
    private void createGameControllerSpy() {
        gameController = spy(new GameController());
    }

    /**
     * Creates a game spy so we can run playGame()
     */
    private void createGameSpy() throws Exception {
        game = new Game(new GameSettings(5000, 50, 25, 2, 10, AIType.MCTS_AI), gameController);
        gameSpy = spy(game);

        doNothing().when(gameSpy, "delay", anyLong());
    }

    @Test
    public void testMockTwoPlayersBothAllIn() throws Exception {
        createGameControllerMock();
        createGameSpy();

        gameSpy.addPlayer("Ragnhild", 0);
        gameSpy.addPlayer("Kristian", 1);

        gameSpy.playGame();
    }
    @Test
    public void testPlayGameWithSixSimpleAIs() throws Exception {
        GameSettings gameSettings = new GameSettings(5000, 50, 25, 6, 10, AIType.SIMPLE_AI);
        gameController = new GameController(gameSettings);
        gameSpy = spy(new Game(gameSettings, gameController));
        doNothing().when(gameSpy, "delay", anyLong());

        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class)).thenReturn(gameSpy);

        Thread gameThread = gameController.initGame(false, new ArrayList<>());
        gameThread.join();
    }

    @Test
    public void testPlayGameWithSixMCTSAIs() throws Exception {
        gameControllerSpy = spy(new GameController());
//        doNothing().when(gameControllerSpy, "delay", anyLong());
        doNothing().when(gameControllerSpy).delay(anyLong());

        gameSpy = spy(new Game(new GameSettings(GameSettings.DEFAULT_SETTINGS), gameControllerSpy));
        doNothing().when(gameSpy, "delay", anyLong());

        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class)).thenReturn(gameSpy);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }
}