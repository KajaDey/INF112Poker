package gamelogic;

import gui.GUIMain;
import gui.GameSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;


/**
 * Created by pokki on 03/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Game.class, GameController.class, GUIMain.class})

public class GameTest {
    private GameController gameController, gameControllerSpy;
    private Game game, gameSpy;

    private GUIMain guiMain = mock(GUIMain.class);
    private GameSettings gameSettings = new GameSettings(5000, 25, 50, 2, 10, AIType.MCTS_AI,30);

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
        gameController = new GameController(guiMain);
        gameControllerSpy = PowerMockito.spy(gameController);

        doNothing().when(gameControllerSpy).showHoleCards(anyObject());
        doNothing().when(gameControllerSpy).preShowdownWinner(anyInt());

        doNothing().when(gameControllerSpy).setDecisionForClient(anyInt(), anyObject());
    }

    /**
     * Creates a game spy so we can run playGame()
     */
    private void createGameSpy() throws Exception {
        game = new Game(gameSettings, gameController);
        gameSpy = PowerMockito.spy(game);

        doNothing().when(gameSpy, "delay", anyLong());
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
        System.out.println("Added one player");
        gameSpy.addPlayer("Kristian", 1);
        System.out.println("added two players");

        gameSpy.playGame();
    }
    @Test
    public void testSpyTwoAIPlayers() throws Exception {
        createGameControllerSpy();
        createGameSpy();


        // add client:
            // gameControllerSpy.addClient()
        // set blinds:
            //  clients.get(clientID).setBigBlind(gameSettings.getBigBlind());
            //  clients.get(clientID).setSmallBlind(gameSettings.getSmallBlind());
        // set names:
            //  clients.get(clientID).setPlayerNames(new HashMap<>(names));


        gameSpy.playGame();
    }

}