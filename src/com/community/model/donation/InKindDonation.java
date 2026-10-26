package com.community.model.donation;

import com.community.model.lookup.DonationType;
import com.community.model.user.User;

public class InKindDonation extends Donation {
    private String itemName;
    private int quantity;

    public InKindDonation(User donor, Campaign campaign, String itemName, int quantity) {
        super(donor, campaign);
        this.type = new DonationType(2, "Inkind");


        this.itemName = itemName;
        this.quantity = quantity;
    }

    // no actual processing just validate
    @Override
    public boolean process(){
        return itemName != null && !itemName.trim().isEmpty()
            && quantity > 0;
    }

    @Override
    public String getReceiptDetails() {
        return String.format(
            "Item       : %s%n"
                + "Quantity   : %d%n",
            itemName, quantity
        );
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public double getAmount() {
        return 0;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
