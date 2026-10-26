package com.community.model.donation;

import com.community.model.lookup.DonationType;
import com.community.model.user.User;

public class OnlineDonation extends Donation{
    private String transactionId;
    private double amount;

    public OnlineDonation(User donor, Campaign campaign, String transactionId, double amount) {
        super(donor, campaign);
        this.type = new DonationType(1, "Online");


        this.transactionId = transactionId;
        this.amount = amount;
    }

    // no actual processing just validate
    @Override
    public boolean process(){
        return transactionId != null && !transactionId.trim().isEmpty()
            && amount > 0;
    }

    @Override
    public String getReceiptDetails() {
        return String.format(
            "Amount     : %,.2f EGP%n"
                + "Transaction: %s%n",
            amount, transactionId
        );
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
