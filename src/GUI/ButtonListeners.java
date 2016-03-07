package GUI;

import javafx.scene.control.ChoiceBox;

/**
 * Created by ady on 07/03/16.
 */
public class ButtonListeners {

    /**
     * What happens when the betButton is pushed
     */
    public static void betButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the checkButton is pushed
     */
    public static void checkButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the doubleButton is pushed
     */
    public static void doubleButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the foldButton is pushed
     */
    public static void foldButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the maxButton is pushed
     */
    public static void maxButtonListener(){
        //TODO: Implement method
    }

    /**
     * What happens when the potButton is pushed
     */
    public static void potButtonListener(){
        //TODO: Implement method
    }

    /**
     *
     */
    public static void mainScreenEnterListener(String name, String numOfPlayers, ChoiceBox<String> choiceBox){
        System.out.println(name);
        System.out.println(numOfPlayers);
        System.out.println(choiceBox.getValue());

    }

}
