package com.community.strategy.notification;

import com.community.model.user.User;

public class SMSStrategy implements NotificationStrategy {
    @Override
    public boolean send(User to, String msg) {
        System.out.println("[SMS to " + to.getName() + "] " + msg);
        return true;
    }
}
