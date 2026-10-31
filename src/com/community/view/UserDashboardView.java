package com.community.view;

import java.util.List;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

import com.community.model.donation.Donation;
import com.community.model.donation.InKindDonation;
import com.community.model.user.User;

// MVC View: shapes the logged-in user's donations into the data the user panel renders.
@Component
public class UserDashboardView {

    // tax added ON TOP of an online/check donation (14%); the campaign still receives the full amount
    private static final double TAX_RATE = 0.14;

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

    // Annual tax statement as plain text. Online and check donations count as
    // deductible; in-kind donations are listed but not valued.
    public String taxStatement(User user, int year, List<Donation> donations) {
        DateTimeFormatter day = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        List<Donation> inYear = donations.stream()
                .filter(d -> d.getDate().getYear() == year)
                .sorted(Comparator.comparing(Donation::getDate).thenComparingInt(Donation::getId))
                .toList();
        List<Donation> taxed = inYear.stream().filter(d -> !(d instanceof InKindDonation)).toList();
        List<Donation> inKind = inYear.stream().filter(d -> d instanceof InKindDonation).toList();
        double donated = taxed.stream().mapToDouble(Donation::getAmount).sum();
        double tax = donated * TAX_RATE;
        long percent = Math.round(TAX_RATE * 100);

        String thick = "=".repeat(40) + "\n";
        String thin = "-".repeat(40) + "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(thick)
                .append("        ANNUAL TAX STATEMENT ").append(year).append("\n")
                .append(thick)
                .append("Donor      : ").append(user.getName()).append("\n")
                .append("Email      : ").append(user.getEmail()).append("\n")
                .append("Issued     : ").append(LocalDate.now().format(day)).append("\n")
                .append(thin)
                .append("ONLINE AND CHECK DONATIONS\n")
                .append("Tax added on top: ").append(percent).append("% of each donation\n");
        if (taxed.isEmpty()) {
            sb.append("  None recorded.\n");
        } else {
            sb.append(String.format(Locale.US, "  %-7s  %-11s  %-12s  %9s  %9s  %9s\n",
                    "No.", "Date", "Campaign", "Donated", "Tax", "Total"));
        }
        for (Donation d : taxed) {
            double t = d.getAmount() * TAX_RATE;
            sb.append(String.format(Locale.US, "  #%06d  %s  %-12s  %9.2f  %9.2f  %9.2f\n",
                    d.getId(), d.getDate().format(day), fit(d.getCampaign().getTitle(), 12),
                    d.getAmount(), t, d.getAmount() + t));
        }
        sb.append(String.format(Locale.US, "%-17s: EGP %.2f\n", "Total donated", donated))
                .append(String.format(Locale.US, "%-17s: EGP %.2f\n", "Tax added (" + percent + "%)", tax))
                .append(String.format(Locale.US, "%-17s: EGP %.2f\n", "Total with tax", donated + tax))
                .append(thin)
                .append("IN-KIND DONATIONS (not valued, no tax added)\n");
        if (inKind.isEmpty()) {
            sb.append("  None recorded.\n");
        }
        for (Donation d : inKind) {
            sb.append(String.format(Locale.US, "  #%06d  %s  %-20s  %s\n",
                    d.getId(), d.getDate().format(day), fit(d.getCampaign().getTitle(), 20),
                    DonationSummary.from(d).details()));
        }
        sb.append(thin)
                .append("This statement lists the donations recorded in the\n")
                .append("system for the year above. Keep it with your receipts\n")
                .append("for your tax records.\n")
                .append(thick);
        return sb.toString();
    }

    private static String fit(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
