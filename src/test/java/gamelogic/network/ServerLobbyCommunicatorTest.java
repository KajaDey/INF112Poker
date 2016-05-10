package gamelogic.network;

import gamelogic.Logger;
import gui.LobbyScreen;
import javafx.application.Platform;
import network.Server;
import network.ServerLobbyCommunicator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Stack;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by Ragnhild Aalvik on 10/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerLobbyCommunicator.class, Platform.class})
public class ServerLobbyCommunicatorTest {

    private ServerLobbyCommunicator lobbyCommunicator;
    public static Stack<String> inputStrings;

    @Test
    public void testServerLobbyCommunicator() throws Exception {
        setupServerLobbyTest();

        lobbyCommunicator.start();

        Thread commThread = lobbyCommunicator.getListeningThread();
        commThread.join();
    }

    private void setupServerLobbyTest() throws Exception {
        new Server(39100);

        inputStrings = new Stack<>();
        inputStrings.addAll(Arrays.asList("startGame", "playerJoinedTable 0 0",
                "tableSettings 0 smallBlind 25 bigBlind 100 maxNumberOfPlayers 2",
                "tableCreated 0", "playerJoinedLobby 0 Ragnhild", "lobbySent", "lobbyok"));

        createServerLobbyCommunicatorSpy();
    }

    private void createServerLobbyCommunicatorSpy() throws Exception {
        try {
            lobbyCommunicator = spy(new ServerLobbyCommunicator("Ragnhild", mock(LobbyScreen.class), InetAddress.getLocalHost(), new Logger()));

            doNothing().when(lobbyCommunicator, "goToGameScreen");

            doAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    if (inputStrings.isEmpty()) System.exit(0);

                    return inputStrings.pop();
                }
            }).when(lobbyCommunicator, "readFromServer");

            mockStatic(Platform.class);
            doNothing().when(Platform.class, "runLater", any(Runnable.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
