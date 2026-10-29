package com.community.model.donation.decorator;
//another one
import com.community.model.donation.Donation;

public class TaxDeductibleDecorator extends DonationDecorator {

    public TaxDeductibleDecorator(Donation wrappee) {
        super(wrappee);
    }

    @Override
    public String generateReceipt() {
        return addReceiptLine(wrappee.generateReceipt(), "Tax       : Deductible donation");
    }
}
