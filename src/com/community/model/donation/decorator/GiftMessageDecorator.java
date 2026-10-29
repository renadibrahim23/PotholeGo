package com.community.model.donation.decorator;
//example decorator
import com.community.model.donation.Donation;

public class GiftMessageDecorator extends DonationDecorator {
    private final String message;

    public GiftMessageDecorator(Donation wrappee, String message) {
        super(wrappee);
        this.message = message;
    }

    @Override
    public String generateReceipt() {
        return addReceiptLine(wrappee.generateReceipt(), "Message    : " + message);
    }
}
