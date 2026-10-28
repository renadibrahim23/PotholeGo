package com.community.view;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.repository.CampaignRepository;

// MVC View: shapes the model into the data the React admin dashboard renders.
@Component
public class AdminDashboardView {
    private final CampaignRepository campaigns;

    public AdminDashboardView(CampaignRepository campaigns) {
        this.campaigns = campaigns;
    }

    public record Dashboard(
            double totalRaised,
            int campaignCount,
            int campaignsFunded,
            int donationCount,
            Map<String, Long> donationsByType,
            List<CampaignSummary> campaigns,
            List<DonationSummary> recentDonations) {
    }

    // re-reads the latest model data and renders it
    public Dashboard refresh() {
        List<Campaign> all = campaigns.findAll();
        return render(all, all.stream().flatMap(c -> c.getDonations().stream()).toList());
    }

    public Dashboard render(List<Campaign> campaigns, List<Donation> donations) {
        Map<String, Long> byType = new LinkedHashMap<>();
        for (Donation d : donations) {
            byType.merge(d.getType().getName(), 1L, Long::sum);
        }

        List<CampaignSummary> summaries = campaigns.stream().map(CampaignSummary::from).toList();

        List<DonationSummary> recent = donations.stream()
                .sorted(Comparator.comparingInt(Donation::getId).reversed())
                .limit(10)
                .map(DonationSummary::from)
                .toList();

        return new Dashboard(
                campaigns.stream().mapToDouble(Campaign::getRaisedAmount).sum(),
                campaigns.size(),
                (int) summaries.stream().filter(CampaignSummary::goalReached).count(),
                donations.size(),
                byType,
                summaries,
                recent);
    }
}
