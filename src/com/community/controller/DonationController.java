package com.community.controller;

import java.util.*;

import com.community.observer.CampaignPublisher;
import com.community.observer.DonorSubscriber;
import com.community.strategy.notification.NotificationSender;
import com.community.model.donation.decorator.GiftMessageDecorator;
import com.community.model.donation.decorator.AnonymousDonationDecorator;
import com.community.strategy.payment.*;
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

    // Strategy: how an online donation is paid (request field "paymentMethod")
    private static final Map<String, PaymentStrategy> PAYMENTS = Map.of(
            "card", new CardPaymentStrategy(),
            "wallet", new WalletPaymentStrategy(),
            "bank", new BankTransferPaymentStrategy());


    private final CampaignRepository campaigns;
    private final DonationRepository repo;
    private final UserRepository users;
    private final DonationView view;
    private final NotificationSender sender;

    public DonationController(CampaignRepository campaigns, DonationRepository repo, UserRepository users,
            DonationView view, NotificationSender sender) {
        this.campaigns = campaigns;
        this.repo = repo;
        this.users = users;
        this.view = view;
        this.sender=sender;
    }

    public record DonationRequest(
            Integer campaignId,
            String type,
            Double amount,
            String transactionId,
            String checkNumber,
            String bankName,
            String itemName,
            Integer quantity,
            String paymentMethod,
            String message,
            Boolean anonymous){
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
                String method = req.paymentMethod() == null || req.paymentMethod().isBlank()
                        ? "card" : req.paymentMethod().trim().toLowerCase();
                PaymentStrategy strategy = PAYMENTS.get(method);
                if (strategy == null) {
                    throw badRequest("Payment method must be Card, Wallet or Bank");
                }
                PaymentResult paid = new PaymentProcessor(strategy).process(donor, req.amount());
                if (!paid.success()) {
                    throw badRequest(paid.message());
                }
                data.put("transactionId", paid.transactionId());
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
        notifyDonors(campaign);
        return new DonationResult(DonationSummary.from(donation), decorate(donation, req).generateReceipt());
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

    // Observer: tell every donor of the campaign how it is doing. A failed
    // notification must never undo a donation that is already saved.
    private void notifyDonors(Campaign campaign) {
        try {
            CampaignPublisher publisher = new CampaignPublisher();
            Set<Integer> seen = new HashSet<>();
            for (Donation d : campaign.getDonations()) {
                if (seen.add(d.getDonor().getId())) {
                    publisher.subscribe(new DonorSubscriber(d.getDonor(), campaign, sender));
                }
            }
            publisher.notifyDonors();
        } catch (RuntimeException e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
    }
    // Decorator: optional extras printed on the receipt
    private static Donation decorate(Donation d, DonationRequest req) {
        Donation result = d;
        if (req.message() != null && !req.message().isBlank()) {
            result = new GiftMessageDecorator(result, req.message().trim());
        }
        if (Boolean.TRUE.equals(req.anonymous())) {
            result = new AnonymousDonationDecorator(result);
        }
        return result;
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
