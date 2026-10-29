package com.community.strategy.notification;

import com.community.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

// Strategy context. A Spring singleton, so the donation code and the admin
// panel share one sender (and therefore one active channel).
@Component
public class NotificationSender {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // synchronized: web requests run on several threads at once
    private static final List<String> LOG = Collections.synchronizedList(new ArrayList<>());

    private static final Map<String, NotificationStrategy> CHANNELS = Map.of(
            "email", new EmailStrategy(),
            "sms", new SMSStrategy(),
            "push", new PushStrategy());

    private volatile NotificationStrategy strategy = CHANNELS.get("email"); // default

    public void setStrategy(NotificationStrategy s) {
        this.strategy = s;
    }

    // switch by name (email, sms, push); false if the name is unknown
    public boolean setChannel(String name) {
        NotificationStrategy s = name == null ? null : CHANNELS.get(name.trim().toLowerCase());
        if (s == null) {
            return false;
        }
        this.strategy = s;
        return true;
    }

    public String getChannel() {
        return strategy.getClass().getSimpleName().replace("Strategy", "");
    }

    public boolean send(User to, String msg) {
        NotificationStrategy current = strategy;
        boolean ok = current.send(to, msg);
        LOG.add("[" + LocalDateTime.now().format(TIME) + "] "
                + current.getClass().getSimpleName().replace("Strategy", "")
                + " -> " + to.getName() + ": " + msg
                + (ok ? "" : " (FAILED)"));
        return ok;
    }

    public static List<String> getLog() {
        synchronized (LOG) {
            return List.copyOf(LOG);
        }
    }
}
