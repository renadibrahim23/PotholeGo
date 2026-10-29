package com.community.observer;

import com.community.model.report.Report;
import com.community.model.user.Volunteer;
import com.community.strategy.notification.NotificationSender;

public class VolunteerSubscriber implements Subscriber {
    private final Volunteer volunteer;
    private final Report report;
    private final NotificationSender sender;

    public VolunteerSubscriber(Volunteer volunteer, Report report, NotificationSender sender) {
        this.volunteer = volunteer;
        this.report = report;
        this.sender = sender;
    }

    @Override
    public void update() {
        sender.send(volunteer, "Report \"" + report.getTitle()
                + "\" you are assigned to is now: " + report.getStatus().getName());
    }
}
