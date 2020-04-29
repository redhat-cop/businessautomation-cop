package org.bala.drink.coffee.model;

import java.io.Serializable;

/**
 * Order
 */
public class DrinkOrder implements Serializable {

    private static final long serialVersionUID = -1317815680255012929L;

    private String drinkType;

    private String cupSize;

    private CardPayment cardPayment;

    private double orderPrice;

    public String getDrinkType() {
        return drinkType;
    }

    public CardPayment getCardPayment() {
        return cardPayment;
    }

    public void setCardPayment(CardPayment cardPayment) {
        this.cardPayment = cardPayment;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }

    public String getCupSize() {
        return cupSize;
    }

    public void setCupSize(String cupSize) {
        this.cupSize = cupSize;
    }

    public double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }

    public enum CupSizeEnum {
        SMALL, MEDIUM, LARGE
    }


}
