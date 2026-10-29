package com.community.strategy.payment;

import java.util.UUID;

import com.community.model.user.User;

// simulated: no real gateway, it just approves and issues a reference
public class WalletPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentResult pay(User payer, double amount) {
        String txn = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txn, "Mobile wallet payment approved");
    }
}
