package gamelogic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Kristian Rosland on 27.04.2016.
 */
public class Server {

    public static ServerSocket serverSocket;
    public static ArrayList<Socket> clientSockets = new ArrayList<>();

    static {
        try {
            serverSocket = new ServerSocket(39100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String ... args){
        Thread server = new Thread("NewConnectionListener"){
            @Override
            public void run() {
                while(true) {
                    try {
                        System.out.println("Server listening for connection..");
                        Socket socket = serverSocket.accept();
                        System.out.println("Connection established with " + socket.getInetAddress());

                        clientSockets.add(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        server.start();
    }

}
