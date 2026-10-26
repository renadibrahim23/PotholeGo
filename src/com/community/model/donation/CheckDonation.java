package com.community.model.donation;

import com.community.model.lookup.DonationType;
import com.community.model.user.User;

public class CheckDonation extends Donation{
    private String checkNumber;
    private String bankName;
    private double amount;

    public CheckDonation(User donor, Campaign campaign, String checkNumber, String bankName, double amount) {
        super(donor, campaign);
        this.type = new DonationType(0, "Check");

        this.checkNumber = checkNumber;
        this.bankName = bankName;
        this.amount = amount;
    }

    // no actual processing just validate
    @Override
    public boolean process(){
        return checkNumber != null && !checkNumber.trim().isEmpty()
        && bankName != null && !bankName.trim().isEmpty()
        && amount > 0;
    }

    @Override
    public String getReceiptDetails() {
        return String.format(
            "Amount     : %,.2f EGP%n"
                + "Check No   : %s%n"
                + "Bank       : %s%n",
            amount, checkNumber, bankName
        );
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public String getBankName() {
        return bankName;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
