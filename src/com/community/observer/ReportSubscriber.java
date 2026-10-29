package com.community.observer;


import com.community.model.report.Report;
import com.community.model.user.Resident;
import com.community.strategy.notification.NotificationSender;

public class ReportSubscriber implements Subscriber {
    private final Resident resident;
    private final Report report;
    private final NotificationSender sender;

    public ReportSubscriber(Resident resident, Report report, NotificationSender sender) {
        this.resident = resident;
        this.report = report;
        this.sender = sender;
    }

    @Override
    public void update() {
        sender.send(resident, "Your report \"" + report.getTitle()
               + "\" is now: " + report.getStatus().getName());
    }
}