package com.community.view;

import com.community.model.donation.Campaign;

// What the UI shows for one campaign (avoids serializing Campaign <-> Donation cycles).
public record CampaignSummary(
        int id,
        String title,
        double goalAmount,
        double raisedAmount,
        double progress,
        int donationCount,
        boolean goalReached) {

    public static CampaignSummary from(Campaign c) {
        double progress = Math.min(100, Math.round(c.getProgress() * 10) / 10.0);
        return new CampaignSummary(
                c.getId(),
                c.getTitle(),
                c.getGoalAmount(),
                c.getRaisedAmount(),
                progress,
                c.getDonations().size(),
                c.getRaisedAmount() >= c.getGoalAmount());
    }
}
