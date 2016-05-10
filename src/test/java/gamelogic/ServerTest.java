package gamelogic;

import gui.GUIMain;
import gui.LobbyScreen;
import javafx.application.Platform;
import network.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by morten on 27.04.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerGameCommunicator.class, Platform.class})

public class ServerTest {

    ServerLobbyCommunicator lobbyCommunicator;
    public static Stack<String> inputStrings;
    ArrayList<Socket> clientSockets;
    ArrayList<BufferedReader> readers;
    ArrayList<BufferedWriter> writers;
    InetSocketAddress socketAddress;
    Stack<String> names;

    @Test
    public void testHandshakeWithServer() throws Exception {
        connectClientsToServer(4);

        assertClientsNotifiedWhenNewPlayerJoined(4);
    }

    @Test
    public void testCreateTable() throws Exception {
        connectClientsToServer(4);
        readAndIgnoreAnswersWhenPlayersJoined(4);

        writeToSocket(0, "createTable");
        assertWhenTableCreatedCorrectAnswersReceived();

        writeToSocket(0, "createTable"); // Tries to create second table
        assertErrorMessageWhenAlreadySeatedAtATable();
    }

    @Test
    public void testLeaveSeat() throws Exception {
        connectClientsToServer(4);
        readAndIgnoreAnswersWhenPlayersJoined(4);

        writeToSocket(0, "leaveSeat");
    }

    @Test
    public void testIllegalMessageSent() throws Exception {
        connectClientsToServer(2);
        System.out.println("Conected clients");
        readAndIgnoreAnswersWhenPlayersJoined(2);

        writeToSocket(0, "Something illegal");

        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();
        assertEquals("Unknown command", answer[1]);
    }

    private void assertErrorMessageWhenAlreadySeatedAtATable() throws IOException {
        String answer = readFromSocket(0);
         // TODO fix



        assert answer.equals("errorMessage \"You are already seated at a table\"") :
                "Didn't get error message when trying to create table while already seated at another table. Got message: "+answer;

        writeToSocket(0, "takeSeat 0");
        answer = readFromSocket(0);
        assert answer.equals("errorMessage \"You are already seated at a table\"") :
                "Didn't get error message when trying to take seat at a table you are already seated on.";
    }

    private void assertWhenTableCreatedCorrectAnswersReceived() throws IOException {
        String [] answer = UpiUtils.tokenize(readFromSocket(0)).get();

        assertEquals(answer[0], "tableCreated");
        assertEquals(answer[1], "0");

        assertEquals(readFromSocket(0), "tableSettings 0 maxNumberOfPlayers 6 startStack 5000 smallBlind 25 bigBlind 50 levelDuration 10 playerClock 30");

        assertEquals(readFromSocket(0), "playerJoinedTable 0 0");
    }

    private void assertClientsNotifiedWhenNewPlayerJoined(int numClients) throws IOException {
        for (int i = 0; i < numClients; i++) {
            for (int j = i; j < numClients; j++) {
                String answer = readFromSocket(i);
                assert answer.startsWith("playerJoinedLobby " + j) : "Got message "+ answer + ", expected playerJoinedLobby "+ j;
            }
        }
    }

    private void readAndIgnoreAnswersWhenPlayersJoined(int numClients) throws Exception {
        for (int i = 0; i < numClients; i++) {
            for (int j = i; j < numClients; j++) {
                readFromSocket(i);
            }
        }
    }

    private void connectClientsToServer(int numClients) throws Exception {
        socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 39100);
        clientSockets = new ArrayList<>();
        readers = new ArrayList<>();
        writers = new ArrayList<>();
        names = new Stack();
        names.addAll(Arrays.asList("Kaja", "Simon", "Vegar", "Ragnhild", "Kristian", "Morten", "Mariah"));

        new Server();
        connectClients(numClients);
    }

    /**
     * Connects given number of client sockets to server socket, and makes handshake
     * @param numClients
     */
    private void connectClients(int numClients) throws Exception {
        for (int i = 0; i < numClients; i++) {
            Socket clientSocket = new Socket();
            clientSocket.connect(socketAddress, 1000);

            clientSockets.add(clientSocket);
            readers.add(new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8")));
            writers.add(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8")));

            String name = names.pop();
            writeToSocket(i, "lobby "+ name);

            String answer = readFromSocket(i);
            assert answer.equals("lobbyok") : "Got handshake "+ answer +", expected lobbyok";

            answer = readFromSocket(i);
            assert answer.equals("yourId "+ i) : "Got handshake "+ answer +", expected yourId "+ i;

            answer = readFromSocket(i);
            assert answer.startsWith("playerNames") : "Got handshake "+ answer +", expected playerNames";

            answer = readFromSocket(i);
            assert answer.equals("lobbySent") : "Got handshake "+ answer + ", expected lobbySent";

            Thread.sleep(100L); // To avoid race conditions
        }
    }

    private String readFromSocket(int clientID) throws IOException {
        String input = readers.get(clientID).readLine();
        return input.trim();
    }

    /**
     * Write to socket (adds new line)
     * @param output Message to write
     */
    private void writeToSocket(int clientID, String output) {
        try {
            writers.get(clientID).write(output + "\n");
            writers.get(clientID).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServerLobbyCommunicator() throws Exception {
        setupServerLobbyTest();

        lobbyCommunicator.start();

        Thread commThread = lobbyCommunicator.getListeningThread();
        commThread.join();
    }

    private void setupServerLobbyTest() throws Exception {
        new Server();

        inputStrings = new Stack<>();
        inputStrings.addAll(Arrays.asList("startGame", "playerJoinedTable 0 0",
                "tableSettings 0 smallBlind 25 bigBlind 100 maxNumberOfPlayers 2",
                "tableCreated 0", "playerJoinedLobby 0 Ragnhild", "lobbySent", "lobbyok"));

        createServerLobbyCommunicatorSpy();
    }

    private void createServerLobbyCommunicatorSpy() throws Exception {
        try {
            lobbyCommunicator = spy(new ServerLobbyCommunicator("Ragnhild", mock(LobbyScreen.class), InetAddress.getLocalHost(), GUIMain.guiMain.logger));

            doNothing().when(lobbyCommunicator, "goToGameScreen");

            doAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    if (inputStrings.isEmpty()) System.exit(0);

                    return ServerTest.inputStrings.pop();
                }
            }).when(lobbyCommunicator, "readFromServer");

            mockStatic(Platform.class);
            doNothing().when(Platform.class, "runLater", any(Runnable.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}