package com.community.observer;

import com.community.model.donation.Campaign;
import com.community.model.user.User;
import com.community.strategy.notification.NotificationSender;

public class DonorSubscriber implements Subscriber{
    private User donor;
    private Campaign campaign;
    private NotificationSender sender;

    @Override
    public void update() {
        String msg = String.format(
            "Campaign \"%s\" has raised %.2f of %.2f (%.0f%% of the goal). Thank you for supporting it!",
            campaign.getTitle(),
            campaign.getRaisedAmount(),
            campaign.getGoalAmount(),
            campaign.getProgress());

        sender.send(donor, msg);
    }
}
