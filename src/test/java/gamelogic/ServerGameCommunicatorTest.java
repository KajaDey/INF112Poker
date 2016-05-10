package gamelogic;

import gamelogic.ai.MCTSAI;
import gui.GUIMain;
import gui.GameSettings;
import network.Server;
import network.ServerGameCommunicator;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by Ragnhild Aalvik on 07/05/16.
 */
public class ServerGameCommunicatorTest {

    ServerGameCommunicator gameCommunicator;


    private void createServerGameCommunicatorSpy(BufferedWriter socketOutput, BufferedReader socketInput, String playerName) {
        gameCommunicator = spy(new ServerGameCommunicator(socketOutput, socketInput, playerName, GUIMain.guiMain.logger));

        try {
            doAnswer((Answer<Optional<GameClient>>) arg -> {
                int userID = ((int) arg.getArguments()[1]);
                return Optional.of(new MCTSAI(userID, GUIMain.guiMain.logger));
            }).when(gameCommunicator, "createGUIClient", any(GameSettings.class), anyInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}