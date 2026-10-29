package com.community.strategy.notification;

import com.community.model.user.User;

public class PushStrategy implements NotificationStrategy {
    @Override
    public boolean send(User to, String msg) {
        System.out.println("[PUSH to " + to.getName() + "] " + msg);
        return true;
    }
}
