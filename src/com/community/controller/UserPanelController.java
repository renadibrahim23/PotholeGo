package com.community.controller;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.community.model.donation.Donation;
import com.community.model.user.User;
import com.community.repository.CampaignRepository;
import com.community.repository.UserRepository;
import com.community.view.UserDashboardView;

import jakarta.servlet.http.HttpSession;

// The logged-in user's own panel.
@RestController
@RequestMapping("/api/user")
public class UserPanelController {
    private final UserRepository users;
    private final CampaignRepository campaigns;
    private final DonationController donations;
    private final UserDashboardView dashboardView;
    private final BCryptPasswordEncoder encoder;

    public UserPanelController(UserRepository users, CampaignRepository campaigns,
            DonationController donations, UserDashboardView dashboardView, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.campaigns = campaigns;
        this.donations = donations;
        this.dashboardView = dashboardView;
        this.encoder = encoder;
    }

    public record ProfileRequest(String name, String email, String currentPassword, String newPassword) {
    }

    @GetMapping("/donations")
    public UserDashboardView.Dashboard myDonations(HttpSession session) {
        User u = AuthController.requireUser(session, users);
        return dashboardView.render(u, donations.history(u));
    }

    @GetMapping(value = "/donations/{id}/receipt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String receipt(@PathVariable int id, HttpSession session) {
        User u = AuthController.requireUser(session, users);
        Donation d = campaigns.findDonationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation not found"));
        if (d.getDonor().getId() != u.getId() && !AuthController.isAdmin(u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your donation");
        }
        return d.generateReceipt();
    }

    // annual tax statement (plain text); defaults to the current year
    @GetMapping(value = "/tax-statement", produces = MediaType.TEXT_PLAIN_VALUE)
    public String taxStatement(@RequestParam(required = false) Integer year, HttpSession session) {
        User u = AuthController.requireUser(session, users);
        int y = year == null ? LocalDate.now().getYear() : year;
        return dashboardView.taxStatement(u, y, donations.history(u));
    }

    @PutMapping("/profile")
    public AuthController.UserInfo editProfile(@RequestBody ProfileRequest req, HttpSession session) {
        User u = AuthController.requireUser(session, users);
        if (req.name() == null || req.name().isBlank()
                || req.email() == null || !req.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and a valid email are required");
        }
        users.findByEmail(req.email())
                .filter(other -> other.getId() != u.getId())
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already used by another account");
                });
        if (req.newPassword() != null && !req.newPassword().isEmpty()) {
            if (req.currentPassword() == null || !encoder.matches(req.currentPassword(), u.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is wrong");
            }
            if (req.newPassword().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
            }
            u.setPasswordHash(encoder.encode(req.newPassword()));
        }
        u.setName(req.name().trim());
        u.setEmail(req.email().trim());
        users.save(u);
        return AuthController.UserInfo.from(u);
    }
}
