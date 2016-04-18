package gui.layouts;

import javafx.scene.image.Image;

/**
 * Created by Kristian Rosland on 17.04.2016.
 *
 * Interface for GUI Player layouts.
 */
public interface IPlayerLayout {

    void setLastMove(String lastMove, Image chipImage);

    void setPositionLabel(String pos, Image buttonImage);

    void setStackLabel(String stack);

    void setCardImage(Image leftImage, Image rightImage);

    void setNameLabel(String name);

    void bustPlayer(String bustedText);

    boolean isBust();

    void foldPlayer();

    void highlightTurn(boolean highlight);

}
