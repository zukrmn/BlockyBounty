package com.blockycraft.blockybounty.data;

public class Bounty {

    private final String setterUUID;
    private final String setterName;
    private final String targetUUID;
    private final String targetName;
    private final int amount;
    private final long createdAt;

    public Bounty(String setterUUID, String setterName, String targetUUID, String targetName, int amount, long createdAt) {
        this.setterUUID = setterUUID;
        this.setterName = setterName;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getSetterUUID() {
        return setterUUID;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getAmount() {
        return amount;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
