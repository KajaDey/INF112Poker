package gamelogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by ady on 05/05/16.
 */
public class ReplayReader {

    private InfoType currentType = null;

    public enum InfoType{
        COMMUNITY,HOLD,DECISION,SETTINGS
    }

    public void readFile(String fileName){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            String line = br.readLine();
            switch (line){
                case "COMMUNITY CARDS":
                    currentType = InfoType.COMMUNITY;
                    break;
                case "CARDS":
                    currentType = InfoType.HOLD;
                    break;
                case "SETTINGS":
                    currentType = InfoType.SETTINGS;
                    break;
                case "DECISIONS":
                    currentType = InfoType.DECISION;
                    break;
                default:
                    break;


            }
            switch (currentType){
                case COMMUNITY:
                    break;
                case HOLD:
                    break;
                case SETTINGS:
                    break;
                case DECISION:
                    break;
                default:
                    break;
            }
        }
        catch (IOException e){
            System.out.println(e);
        }
    }
}
