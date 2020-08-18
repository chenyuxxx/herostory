package org.tinygame.herostory.mq;

public class VictorMsg {
    /**
     * 赢家Id
     */
    private int winId;

    /**
     * 输家Id
     */
    private int loserId;

    public int getWinId() {
        return winId;
    }

    public void setWinId(int winId) {
        this.winId = winId;
    }

    public int getLoserId() {
        return loserId;
    }

    public void setLoserId(int loserId) {
        this.loserId = loserId;
    }
}
