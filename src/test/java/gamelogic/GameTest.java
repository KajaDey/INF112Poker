package gamelogic;

import gamelogic.ai.MCTSAI;
import gamelogic.ai.SimpleAI;
import gui.GUIMain;
import gui.GameSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import org.powermock.reflect.Whitebox;


/**
 * Created by pokki on 03/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, GameController.class, GUIMain.class})

public class GameTest {
    private GameController gameController, gameControllerSpy;
    private Game game, gameSpy;
    private GameSettings gameSettings = new GameSettings(5000, 25, 50, 2, 10, AIType.MCTS_AI);

    // mocked object
    private GUIMain guiMain = PowerMockito.mock(GUIMain.class);


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
        gameController = new GameController(gameSettings);
        gameControllerSpy = PowerMockito.spy(gameController);

        doNothing().when(gameControllerSpy).preShowdownWinner(anyInt());

        doAnswer(new Answer<Decision>() {
            @Override
            public Decision answer(InvocationOnMock ID) throws Throwable {
                //Ask for decision from client
                GameClient client = gameControllerSpy.clients.get(ID);
                if (client instanceof SimpleAI || client instanceof MCTSAI)
                    return gameControllerSpy.getAIDecision(client);
                else
                    return client.getDecision(20000L);
            }

        }).when(gameControllerSpy).getDecisionFromClient(anyInt());



    }

    /**
     * Creates a game spy so we can run playGame()
     */
    private void createGameSpy() throws Exception {
        game = new Game(gameSettings, gameController);
        gameSpy = PowerMockito.spy(game);

        doNothing().when(gameSpy, "delay", anyLong());
        doNothing().when(gameSpy, "displayHoleCards");
    }

    @Before
    public void inititalizeMockito() {
        MockitoAnnotations.initMocks(this);
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
    public void testSpyTwoAIPlayers() throws Exception {
        createGameControllerSpy();
        createGameSpy();

        // Add clients
        gameControllerSpy.addClient(0, new MCTSAI(0), "Ragnhild");
        gameControllerSpy.addClient(1, new MCTSAI(1), "Morten");

        // set blinds and names, this invokes the private method initClients:
        Whitebox.invokeMethod(gameControllerSpy, "initClients");

        gameSpy.playGame();


        // override gc.getDecisionFromClient()......
    }
}