package com.oleje.trainplannerapp.domain;

import java.util.UUID;

/**
 * Strategy implementation for dynamic Promo Code percentage discounts (e.g. -10%, -15%).
 */
public class PromoCodeDiscount implements BonusDiscountStrategy {
    private final String id;
    private final double percentage; // e.g. 10.0 for 10%
    private final String code;

    public PromoCodeDiscount(double percentage) {
        this(UUID.randomUUID().toString(), "Промокод -" + (int) percentage + "%", percentage);
    }

    public PromoCodeDiscount(String id, String code, double percentage) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.code = code != null ? code : "Промокод -" + (int) percentage + "%";
        this.percentage = Math.max(0.0, Math.min(100.0, percentage));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return code;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public double calculateSavings(double currentTicketPrice) {
        if (currentTicketPrice <= 0) return 0.0;
        return currentTicketPrice * (percentage / 100.0);
    }
}
