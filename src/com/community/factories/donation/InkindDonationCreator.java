package com.community.factories.donation;
import java.util.Map;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.model.donation.InKindDonation;
import com.community.model.user.User;

public class InkindDonationCreator implements DonationCreator {
    public Donation createDonation(Map<String, Object> data){
        User donor = (User) data.get("donor");
        Campaign campaign = (Campaign) data.get("campaign");
        String itemName = (String) data.get("itemName");
        int quantity = (Integer) data.get("quantity");
        return new InKindDonation(donor, campaign, itemName, quantity);
    }
}