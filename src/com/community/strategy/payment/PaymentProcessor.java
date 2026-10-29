package com.community.strategy.payment;

import com.community.model.user.User;

// Strategy context: picks no method itself, it just runs the one it was given.
public class PaymentProcessor {
    private PaymentStrategy strategy;

    public PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PaymentStrategy s) {
        this.strategy = s;
    }

    public PaymentResult process(User payer, double amount) {
        if (amount <= 0) {
            return new PaymentResult(false, null, "Amount must be greater than zero");
        }
        return strategy.pay(payer, amount);
    }
}
