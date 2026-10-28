package com.community.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.community.factories.donation.CheckDonationCreator;
import com.community.factories.donation.DonationCreator;
import com.community.factories.donation.InkindDonationCreator;
import com.community.factories.donation.OnlineDonationCreator;
import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.model.user.User;
import com.community.repository.CampaignRepository;
import com.community.repository.DonationRepository;
import com.community.repository.UserRepository;
import com.community.view.CampaignSummary;
import com.community.view.DonationSummary;
import com.community.view.DonationView;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class DonationController {

    private static final Map<String, DonationCreator> CREATORS = Map.of(
            "online", new OnlineDonationCreator()::createDonation,
            "check", new CheckDonationCreator()::createDonation,
            "inkind", new InkindDonationCreator());

    private final CampaignRepository campaigns;
    private final DonationRepository repo;
    private final UserRepository users;
    private final DonationView view;

    public DonationController(CampaignRepository campaigns, DonationRepository repo, UserRepository users,
            DonationView view) {
        this.campaigns = campaigns;
        this.repo = repo;
        this.users = users;
        this.view = view;
    }

    public record DonationRequest(
            Integer campaignId,
            String type,
            Double amount,
            String transactionId,
            String checkNumber,
            String bankName,
            String itemName,
            Integer quantity) {
    }

    public record DonationResult(DonationSummary donation, String receipt) {
    }

    // ---- public: landing page and campaign pages ----

    @GetMapping("/campaigns")
    public List<CampaignSummary> listCampaigns() {
        return campaigns.findAll().stream().map(CampaignSummary::from).toList();
    }

    @GetMapping("/campaigns/{id}")
    public DonationView.CampaignPage getCampaign(@PathVariable int id) {
        return view.render(campaigns.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign " + id + " not found")));
    }

    // ---- logged-in donors ----

    @PostMapping("/donations")
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResult donate(@RequestBody DonationRequest req, HttpSession session) {
        User donor = AuthController.requireUser(session, users);

        String type = req.type() == null ? "" : req.type().trim().toLowerCase();
        DonationCreator creator = CREATORS.get(type);
        if (creator == null) {
            throw badRequest("Type must be Online, Check or InKind");
        }
        if (req.campaignId() == null) {
            throw badRequest("Campaign is required");
        }
        Campaign campaign = campaigns.findById(req.campaignId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        Map<String, Object> data = new HashMap<>();
        data.put("donor", donor);
        data.put("campaign", campaign);
        switch (type) {
            case "online" -> {
                requireAmount(req);
                // no real payment yet: the PaymentProcessor strategy will supply this
                String txn = req.transactionId() == null || req.transactionId().isBlank()
                        ? "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                        : req.transactionId().trim();
                data.put("transactionId", txn);
                data.put("amount", req.amount());
            }
            case "check" -> {
                requireAmount(req);
                data.put("checkNumber", req.checkNumber());
                data.put("bankName", req.bankName());
                data.put("amount", req.amount());
            }
            default -> {
                if (req.quantity() == null) {
                    throw badRequest("Quantity is required");
                }
                data.put("itemName", req.itemName());
                data.put("quantity", req.quantity());
            }
        }

        Donation donation = creator.createDonation(data);
        if (!donation.process()) {
            throw badRequest("Donation details are incomplete or invalid");
        }
        campaign.addDonation(donation);
        repo.save(donation);
        // TODO: notify subscribers through CampaignPublisher once it is fixed
        return new DonationResult(DonationSummary.from(donation), donation.generateReceipt());
    }

    @GetMapping("/donations/history")
    public List<DonationSummary> history(HttpSession session) {
        User u = AuthController.requireUser(session, users);
        return history(u).stream().map(DonationSummary::from).toList();
    }

    // shared with UserPanelController
    public List<Donation> history(User u) {
        return campaigns.findAllDonations().stream()
                .filter(d -> d.getDonor().getId() == u.getId())
                .sorted(Comparator.comparingInt(Donation::getId).reversed())
                .toList();
    }

    private static void requireAmount(DonationRequest req) {
        if (req.amount() == null) {
            throw badRequest("Amount is required");
        }
    }

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
