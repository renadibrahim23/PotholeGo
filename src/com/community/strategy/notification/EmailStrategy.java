package com.community.strategy.notification;


import com.community.model.user.User;

public class EmailStrategy implements NotificationStrategy {
    @Override
    public boolean send(User to, String msg) {
        System.out.println("[EMAIL to " + to.getEmail() + "] " + msg);
        return true;
    }
}
