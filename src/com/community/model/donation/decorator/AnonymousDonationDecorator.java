package com.community.model.donation.decorator;
//another one
import com.community.model.donation.Donation;

// Receipt shows "Anonymous" instead of the donor's name.
public class AnonymousDonationDecorator extends DonationDecorator {

    public AnonymousDonationDecorator(Donation wrappee) {
        super(wrappee);
    }

    @Override
    public String generateReceipt() {
        String name = wrappee.getDonor().getName();
        return wrappee.generateReceipt()
                .replace("Donor      : " + name, "Donor      : Anonymous");
    }
}

