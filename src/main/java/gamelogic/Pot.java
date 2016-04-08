package gamelogic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kristianrosland on 16.03.2016.
 *
 * Class to represent the pot at all times.
 */
public class Pot {

    private long potSize;
    private Map<Integer, Long> amountEachPlayerCanClaimOfThePot;

    public Pot() {
        this.potSize = 0;
        this.amountEachPlayerCanClaimOfThePot = new HashMap<>();
    }

    /**
     * TODO write javadoc
     * @return
     */
    public long getPotSize() { return potSize; }

    /**
     * TODO write javadoc
     * @param userID
     * @param amountToAdd
     * @return
     */
    public long addToPot(int userID, long amountToAdd) {
        if (amountEachPlayerCanClaimOfThePot.get(userID) == null)
            amountEachPlayerCanClaimOfThePot.put(userID, 0L);

        amountEachPlayerCanClaimOfThePot.put(userID, amountEachPlayerCanClaimOfThePot.get(userID) + amountToAdd);

        assert amountToAdd >= 0 : "A negative number was added to the pot.";

        potSize += amountToAdd;
        return potSize;
    }

    /**
     * TODO write javadoc
     * @param playerID
     * @return
     */
    public long getSharePotPlayerCanWin(int playerID) {
        final long amountPlayerHasPutIn = amountEachPlayerCanClaimOfThePot.get(playerID);
        long total = 0;

        for (Integer id : amountEachPlayerCanClaimOfThePot.keySet()) {
            long amount = amountEachPlayerCanClaimOfThePot.get(id);
            if (amountPlayerHasPutIn >= amount) { //If player has put >= this player
                total += amount;
                amountEachPlayerCanClaimOfThePot.put(id, 0L);
            } else {
                total += amountPlayerHasPutIn;
                amountEachPlayerCanClaimOfThePot.put(id, amount-amountPlayerHasPutIn);
            }
        }

        potSize -= total;

        return total;
    }


}
