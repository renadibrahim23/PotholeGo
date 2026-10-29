package com.community.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.community.repository.UserRepository;
import com.community.strategy.notification.NotificationSender;

import jakarta.servlet.http.HttpSession;

// Notification log + channel switch for the admin panel.
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationSender sender;
    private final UserRepository users;

    public NotificationController(NotificationSender sender, UserRepository users) {
        this.sender = sender;
        this.users = users;
    }

    public record NotificationLog(String channel, List<String> entries) {
    }

    public record ChannelRequest(String channel) {
    }

    @GetMapping
    public NotificationLog log(HttpSession session) {
        AuthController.requireAdmin(session, users);
        return snapshot();
    }

    @PutMapping("/channel")
    public NotificationLog setChannel(@RequestBody ChannelRequest req, HttpSession session) {
        AuthController.requireAdmin(session, users);
        if (!sender.setChannel(req.channel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Channel must be Email, SMS or Push");
        }
        return snapshot();
    }

    // newest first
    private NotificationLog snapshot() {
        List<String> entries = new ArrayList<>(NotificationSender.getLog());
        Collections.reverse(entries);
        return new NotificationLog(sender.getChannel(), entries);
    }
}
