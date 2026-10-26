package com.community.factories.donation;
import com.community.model.donation.Donation;
import java.util.Map;

public interface DonationCreator {
    public Donation createDonation(Map<String, Object> data);
}