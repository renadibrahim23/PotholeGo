package com.community.model.donation;
import java.time.LocalDate;

import com.community.model.lookup.DonationType;
import com.community.model.user.User;
import java.time.format.DateTimeFormatter;

public abstract class Donation {
    // MORSI TODO
    // fetch the last valid id from postgres db when its implemented
    // instead of using a runtime counter
    private static int serialCounter = 0;

    protected int id;
    protected LocalDate date;
    protected User donor;
    protected Campaign campaign;
    protected DonationType type;

    public Donation(User donor, Campaign campaign){
        this.id = serialCounter++;
        this.date = LocalDate.now();
        this.donor = donor;
        this.campaign = campaign;
    }

    public abstract boolean process();
    public abstract String getReceiptDetails();
    public abstract double getAmount();

    public String generateReceipt() {
        String thick = "========================================\n";
        String thin  = "----------------------------------------\n";

        return thick
            + "            DONATION RECEIPT\n"
            + thick
            + String.format("Receipt No : #%06d%n", id)
            + String.format("Date       : %s%n", date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            + thin
            + String.format("Donor      : %s%n", donor.getName())
            + String.format("Campaign   : %s%n", campaign.getTitle())
            + String.format("Type       : %s%n", type.getName())
            + getReceiptDetails()
            + thin
            + "    Thank you for your generosity!\n"
            + thick;
    }

    public static int getSerialCounter() {
        return serialCounter;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getDonor() {
        return donor;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public DonationType getType() {
        return type;
    }

    public static void setSerialCounter(int serialCounter) {
        Donation.serialCounter = serialCounter;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public void setType(DonationType type) {
        this.type = type;
    }
}
