package com.community.view;

import java.util.List;

import org.springframework.stereotype.Component;

import com.community.model.donation.Donation;
import com.community.model.user.User;

// MVC View: shapes the logged-in user's donations into the data the user panel renders.
@Component
public class UserDashboardView {

    public record Dashboard(
            String name,
            double totalGiven,
            int donationCount,
            int campaignsSupported,
            List<DonationSummary> donations) {
    }

    public Dashboard render(User user, List<Donation> donations) {
        return new Dashboard(
                user.getName(),
                donations.stream().mapToDouble(Donation::getAmount).sum(),
                donations.size(),
                (int) donations.stream().map(d -> d.getCampaign().getId()).distinct().count(),
                donations.stream().map(DonationSummary::from).toList());
    }
}
