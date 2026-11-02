package com.oleje.trainplannerapp.domain;

import java.util.UUID;

/**
 * Strategy implementation for dynamic Certificate discounts (e.g. -200 UAH, -50 UAH).
 */
public class CertificateDiscount implements BonusDiscountStrategy {
    private final String id;
    private final double amountUah;
    private final String code;

    public CertificateDiscount(double amountUah) {
        this(UUID.randomUUID().toString(), "Сертифікат -" + (int) amountUah + " грн", amountUah);
    }

    public CertificateDiscount(String id, String code, double amountUah) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.code = code != null ? code : "Сертифікат -" + (int) amountUah + " грн";
        this.amountUah = Math.max(0.0, amountUah);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return code;
    }

    public double getAmountUah() {
        return amountUah;
    }

    @Override
    public double calculateSavings(double currentTicketPrice) {
        if (currentTicketPrice <= 0) return 0.0;
        return Math.min(currentTicketPrice, amountUah);
    }
}
