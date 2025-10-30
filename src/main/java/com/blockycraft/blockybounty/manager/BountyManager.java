// src/main/java/com/blockycraft/blockybounty/manager/BountyManager.java

package com.blockycraft.blockybounty.manager;

import com.blockycraft.blockybounty.data.Bounty;
import com.blockycraft.blockybounty.database.BountyDatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class BountyManager {

    private final BountyDatabaseManager dbManager;

    public BountyManager(BountyDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void createBounty(String setterUUID, String setterName, String targetUUID, String targetName, int amount) {
        Bounty bounty = new Bounty(setterUUID, setterName, targetUUID, targetName, amount, System.currentTimeMillis() / 1000L);
        dbManager.addBounty(bounty);
    }

    public void removeBounty(String setterUUID, String targetUUID) {
        dbManager.removeBounty(setterUUID, targetUUID);
    }

    public void removeAllBountiesForTarget(String targetUUID) {
        dbManager.removeAllBountiesForPlayer(targetUUID);
    }

    public List<Bounty> getBountiesForTarget(String targetUUID) {
        return dbManager.getBountiesForTarget(targetUUID);
    }

    public List<Bounty> getBountiesBySetter(String setterUUID) {
        return dbManager.getBountiesBySetter(setterUUID);
    }

    public List<Bounty> getAllBounties() {
        return dbManager.getAllBounties();
    }

    public int getTotalBountyValue(String targetUUID) {
        int total = 0;
        for (Bounty bounty : getBountiesForTarget(targetUUID)) {
            total += bounty.getAmount();
        }
        return total;
    }

    public List<Bounty> getRemovableBountiesForSetter(String setterUUID, String targetUUID) {
        List<Bounty> result = new ArrayList<Bounty>();
        for (Bounty bounty : getBountiesForTarget(targetUUID)) {
            if (bounty.getSetterUUID().equals(setterUUID)) {
                result.add(bounty);
            }
        }
        return result;
    }
}
