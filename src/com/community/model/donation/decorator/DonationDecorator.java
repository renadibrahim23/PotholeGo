package com.community.model.donation.decorator;

import java.time.LocalDate;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.model.lookup.DonationType;
import com.community.model.user.User;

// Decorator base: wraps a Donation and forwards everything to it.
// Only the receipt is decorated. Save the ORIGINAL donation to the database
// (DonationRepository checks the concrete class), then wrap it for the receipt.
public abstract class DonationDecorator extends Donation {
    protected final Donation wrappee;

    private static final String FOOTER = "-".repeat(40) + "\n    Thank you for your generosity!";

    public DonationDecorator(Donation wrappee) {
        super(wrappee.getDonor(), wrappee.getCampaign());
        this.wrappee = wrappee;
    }

    @Override public boolean process() { return wrappee.process(); }
    @Override public String getReceiptDetails() { return wrappee.getReceiptDetails(); }
    @Override public double getAmount() { return wrappee.getAmount(); }
    @Override public String generateReceipt() { return wrappee.generateReceipt(); }

    @Override public int getId() { return wrappee.getId(); }
    @Override public LocalDate getDate() { return wrappee.getDate(); }
    @Override public User getDonor() { return wrappee.getDonor(); }
    @Override public Campaign getCampaign() { return wrappee.getCampaign(); }
    @Override public DonationType getType() { return wrappee.getType(); }

    @Override public void setId(int id) { wrappee.setId(id); }
    @Override public void setDate(LocalDate date) { wrappee.setDate(date); }
    @Override public void setDonor(User donor) { wrappee.setDonor(donor); }
    @Override public void setCampaign(Campaign campaign) { wrappee.setCampaign(campaign); }
    @Override public void setType(DonationType type) { wrappee.setType(type); }

    // puts an extra line on the receipt, just above the "Thank you" footer
    protected static String addReceiptLine(String receipt, String line) {
        return receipt.contains(FOOTER)
                ? receipt.replace(FOOTER, line + "\n" + FOOTER)
                : receipt + line + "\n";
    }
}

