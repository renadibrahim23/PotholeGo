package com.community.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.model.lookup.Role;
import com.community.model.user.User;
import com.community.repository.CampaignRepository;
import com.community.repository.DonationRepository;
import com.community.repository.UserRepository;
import com.community.view.AdminDashboardView;
import com.community.view.CampaignSummary;

import jakarta.servlet.http.HttpSession;

// Every endpoint here requires an Admin session.
@RestController
@RequestMapping("/api/admin")
public class AdminPanelController {
    private final CampaignRepository campaigns;
    private final DonationRepository donations;
    private final UserRepository users;
    private final AdminDashboardView dashboardView;

    public AdminPanelController(CampaignRepository campaigns, DonationRepository donations,
            UserRepository users, AdminDashboardView dashboardView) {
        this.campaigns = campaigns;
        this.donations = donations;
        this.users = users;
        this.dashboardView = dashboardView;
    }

    public record CampaignRequest(String title, Double goalAmount) {
    }

    public record RoleRequest(String role) {
    }

    // ---- viewStatistics ----

    @GetMapping("/statistics")
    public AdminDashboardView.Dashboard viewStatistics(HttpSession session) {
        AuthController.requireAdmin(session, users);
        return dashboardView.refresh();
    }

    // ---- manageCampaigns ----

    @PostMapping("/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignSummary createCampaign(@RequestBody CampaignRequest req, HttpSession session) {
        AuthController.requireAdmin(session, users);
        validate(req);
        return CampaignSummary.from(campaigns.create(req.title().trim(), req.goalAmount()));
    }

    @PutMapping("/campaigns/{id}")
    public CampaignSummary updateCampaign(@PathVariable int id, @RequestBody CampaignRequest req,
            HttpSession session) {
        AuthController.requireAdmin(session, users);
        validate(req);
        Campaign c = findCampaign(id);
        c.setTitle(req.title().trim());
        c.setGoalAmount(req.goalAmount());
        return CampaignSummary.from(campaigns.update(c));
    }

    @DeleteMapping("/campaigns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCampaign(@PathVariable int id, HttpSession session) {
        AuthController.requireAdmin(session, users);
        findCampaign(id);
        if (donations.existsForCampaign(id)) {
            // keep donation history and receipts valid
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Campaign has donations and cannot be deleted");
        }
        campaigns.deleteById(id);
    }

    // e.g. a check that bounced, or an in-kind pledge that never arrived
    @DeleteMapping("/donations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDonation(@PathVariable int id, HttpSession session) {
        AuthController.requireAdmin(session, users);
        if (!donations.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Donation " + id + " not found");
        }
    }

    // ---- manageUsers ----

    @GetMapping("/users")
    public List<AuthController.UserInfo> manageUsers(HttpSession session) {
        AuthController.requireAdmin(session, users);
        return users.findAll().stream().map(AuthController.UserInfo::from).toList();
    }

    @PutMapping("/users/{id}/role")
    public AuthController.UserInfo changeRole(@PathVariable int id, @RequestBody RoleRequest req,
            HttpSession session) {
        User me = AuthController.requireAdmin(session, users);
        if (me.getId() == id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }
        User u = users.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
        if (!Role.ADMIN.equalsIgnoreCase(req.role()) && !Role.RESIDENT.equalsIgnoreCase(req.role())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be Admin or Resident");
        }
        users.changeRole(u, users.role(req.role()));
        return AuthController.UserInfo.from(u);
    }

    private Campaign findCampaign(int id) {
        return campaigns.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign " + id + " not found"));
    }

    private static void validate(CampaignRequest req) {
        if (req.title() == null || req.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (req.goalAmount() == null || req.goalAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal amount must be greater than 0");
        }
    }
}
