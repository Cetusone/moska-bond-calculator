package org.example.moskacalculatorservice;

public enum Currency {
    EUR,
    USD,
    CNY,
    RUB;

    public static Currency getCurrency(String currency) {

        if (currency == null) return null; // Защита от NPE

        return switch (currency.toUpperCase().trim()) {
            case "EUR" -> EUR;
            case "USD" -> USD;
            case "CNY" -> CNY;
            case "RUB" -> RUB;
            default -> throw new IllegalArgumentException("Unknown currency: " + currency);
        };
    }
}