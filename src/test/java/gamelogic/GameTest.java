package gamelogic;

import gui.GUIMain;
import gui.GameSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;


import static org.mockito.Mockito.*;

/**
 * Created by pokki on 03/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameTest {
    private GameController gameController, gameControllerSpy;
    private Game game, gameSpy;

    private GUIMain guiMain = mock(GUIMain.class);
    private GameSettings gameSettings = new GameSettings(5000, 25, 50, 2, 10, AIType.MCTS_AI);

    /**
     * Creates a mock game controller for use when testing hard coded decision
     */
    private void createMockGameController() {
        gameController = mock(GameController.class);
        PowerMockito.doReturn(new Decision(Decision.Move.ALL_IN)).when(gameController).getDecisionFromClient(anyInt());
    }

    /**
     * Creates a game controller spy for use when there are AI players
     */
    private void createSpyGameController() {
        gameController = new GameController(guiMain);
        gameControllerSpy = spy(gameController);

        Mockito.doNothing().when(gameControllerSpy).showHoleCards(anyObject());
        Mockito.doNothing().when(gameControllerSpy).preShowdownWinner(anyInt());



        Mockito.doNothing().when(gameControllerSpy).setDecisionForClient(anyInt(), anyObject());

    }

    /**
     * Creates a game spy so we can run playGame()
     */
    private void createGameSpy() {
        game = new Game(gameSettings, gameController);
        gameSpy = spy(game);

//        Mockito.doNothing().when(gameSpy).
    }

    @Before
    public void inititalizeMockito() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMockTwoPlayersBothAllIn() {
        createMockGameController();
        createGameSpy();

        gameSpy.addPlayer("Pokki", 0);
        gameSpy.addPlayer("Kristian", 1);

        gameSpy.playGame();

    }
    @Test
    public void testSpyTwoAIPlayers() {
        createSpyGameController();
        createGameSpy();

        // add client:
            // gameControllerSpy.addClient()
        // set blinds:
            //  clients.get(clientID).setBigBlind(gameSettings.getBigBlind());
            //  clients.get(clientID).setSmallBlind(gameSettings.getSmallBlind());
        // set names:
            //  clients.get(clientID).setPlayerNames(new HashMap<>(names));






    }

}