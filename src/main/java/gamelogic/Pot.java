package gamelogic;

/**
 * Created by kristianrosland on 16.03.2016.
 */
public class Pot {

    private long potSize;

    public Pot() {
        this.potSize = 0;
    }

    public long getPotSize() { return potSize; }
    public synchronized long addToPot(long amountToAdd) {
        System.out.println("Incrementing pot with " + amountToAdd);
        potSize += amountToAdd;
        return potSize;
    }


}
