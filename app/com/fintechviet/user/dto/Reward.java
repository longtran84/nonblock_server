package com.fintechviet.user.dto;

/**
 * Created by tungn on 9/18/2017.
 */
public class Reward {
    private String rewardCode;
    private String rewardName;
    private long amount;

    public String getRewardCode() {
        return rewardCode;
    }

    public void setRewardCode(String rewardCode) {
        this.rewardCode = rewardCode;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
