package com.community.strategy.notification;

import com.community.model.user.User;

public interface NotificationStrategy {
    boolean send(User to, String msg);
}
