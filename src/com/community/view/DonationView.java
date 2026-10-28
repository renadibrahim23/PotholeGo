package com.community.view;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;

// MVC View: shapes one campaign and its donations for the campaign page and donate form.
@Component
public class DonationView {

    public record CampaignPage(CampaignSummary campaign, List<DonationSummary> donations) {
    }

    public CampaignPage render(Campaign campaign) {
        List<DonationSummary> rows = campaign.getDonations().stream()
                .sorted(Comparator.comparingInt(Donation::getId).reversed())
                .map(DonationSummary::from)
                .toList();
        return new CampaignPage(CampaignSummary.from(campaign), rows);
    }
}
