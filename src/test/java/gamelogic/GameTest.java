package gamelogic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import gui.GUIMain;
import gui.GameSettings;
import org.powermock.reflect.Whitebox;
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

    @Test
    public void testGameSpyTwoPlayersBothAllIn() throws Exception {
        gameControllerMock = mock(GameController.class);
        PowerMockito.doReturn(new Decision(Decision.Move.ALL_IN)).when(gameControllerMock).getDecisionFromClient(anyInt());

        gameSpy = spy(new Game(new GameSettings(5000, 50, 25, 2, 10, AIType.MCTS_AI), gameControllerMock));
        doNothing().when(gameSpy, "delay", anyLong());

        gameSpy.addPlayer("Ragnhild", 0);
        gameSpy.addPlayer("Kristian", 1);

        gameSpy.playGame();
    }
    @Test
    public void testPlayGameWithSixSimpleAIs() throws Exception {
        GameSettings gameSettings = new GameSettings(5000, 500, 250, 6, 10, AIType.SIMPLE_AI);
        gameControllerSpy = spy(new GameController(gameSettings));
        doNothing().when(gameControllerSpy).delay(anyLong());

        gameSpy = spy(new Game(gameSettings, gameControllerSpy));
        doNothing().when(gameSpy, "delay", anyLong());
        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class)).thenReturn(gameSpy);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }

    @Test
    public void testPlayGameWithSixMCTSAIs() throws Exception {
        gameControllerSpy = spy(new GameController());

//        doAnswer(new Answer<Decision>() {
//            @Override
//            public Decision answer(InvocationOnMock aiClient) throws Throwable{
//                return null;//(GameClient)aiClient.getDecision(timeToTake);
//            }
//        }).when(gameControllerSpy).getAIDecision(anyObject());
//


        // override getAIDecision
        doNothing().when(gameControllerSpy).delay(anyLong());

        gameSpy = spy(new Game(new GameSettings(GameSettings.DEFAULT_SETTINGS), gameControllerSpy));
        doNothing().when(gameSpy, "delay", anyLong());

        whenNew(Game.class).withArguments(any(GameSettings.class), any(GameController.class)).thenReturn(gameSpy);

        Thread gameThread = gameControllerSpy.initGame(false, new ArrayList<>());
        gameThread.join();
    }
}