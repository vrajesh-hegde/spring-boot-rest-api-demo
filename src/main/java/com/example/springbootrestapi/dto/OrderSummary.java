package com.example.springbootrestapi.dto;

/**
 * Summary view of an order for API responses (id, total price, status with human-readable label).
 */
public class OrderSummary {

    private Long orderId;
    private double totalPrice;
    private String statusCode;
    private String statusLabel;

    public OrderSummary() {}

    public OrderSummary(Long orderId, double totalPrice, String statusCode, String statusLabel) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.statusCode = statusCode;
        this.statusLabel = statusLabel;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
}
