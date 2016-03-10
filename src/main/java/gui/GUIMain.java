package main.java.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.gamelogic.Card;
import main.java.gamelogic.GameController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ady on 05/03/16.
 */
public class GUIMain extends Application{

    private GameController gamecontroller;

    public GUIMain() {
        this.gamecontroller = new GameController(this);
    }

    public static void main(String[] args) {
        GUIMain gui = new GUIMain();
        launch(args);
    }

    //TODO: DEPRECATED
    public static void run(String[] args){
        launch(args);
    }

    public void start(Stage window) throws Exception {
        //TODO: Stop using fucking kake and katt.........
        GUIClient kake = new GUIClient(0);

        //TODO: Get this to take gamecontroller as a parameter instead of GUIClient
        SceneBuilder.showCurrentScene(SceneBuilder.createSceneForInitialScreen("PokerTable"), "Main Screen");


        Map<Integer,Long> map = new HashMap<>();
        Map<Integer,Integer> map2 = new HashMap<>();
        Map<Integer,String> map3 = new HashMap<>();

        Card card1 = Card.of(5, Card.Suit.DIAMONDS).get();
        Card card2 = Card.of(10, Card.Suit.CLUBS).get();

        map.put(0, 0L);
        map.put(1, 0L);

        map2.put(0,0);
        map2.put(1,2);

        map3.put(0,"Kake");
        map3.put(1,"Katt");

        kake.setBigBlind(50);
        kake.setSmallBlind(25);
        //kake.setPot(0);
        kake.setStackSizes(map);
        kake.setHoleCards(card1, card2);
        kake.setPositions(map2);
        kake.setName(map3);
        kake.setAmountOfPlayers(2);
        kake.setLevelDuration(10);
        //kake.setStartChips(10000);

    }
}
