package com.community.view;

import java.time.LocalDate;

import com.community.model.donation.CheckDonation;
import com.community.model.donation.Donation;
import com.community.model.donation.InKindDonation;
import com.community.model.donation.OnlineDonation;

// What the UI shows for one donation in a table row.
public record DonationSummary(
        int id,
        LocalDate date,
        String donorName,
        int campaignId,
        String campaignTitle,
        String type,
        double amount,
        String details) {

    public static DonationSummary from(Donation d) {
        return new DonationSummary(
                d.getId(),
                d.getDate(),
                d.getDonor().getName(),
                d.getCampaign().getId(),
                d.getCampaign().getTitle(),
                d.getType().getName(),
                d.getAmount(),
                details(d));
    }

    private static String details(Donation d) {
        if (d instanceof OnlineDonation o) {
            return "Transaction " + o.getTransactionId();
        }
        if (d instanceof CheckDonation c) {
            return "Check " + c.getCheckNumber() + " (" + c.getBankName() + ")";
        }
        if (d instanceof InKindDonation k) {
            return k.getQuantity() + " x " + k.getItemName();
        }
        return "";
    }
}
