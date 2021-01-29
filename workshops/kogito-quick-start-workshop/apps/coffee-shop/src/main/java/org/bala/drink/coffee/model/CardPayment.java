package org.bala.drink.coffee.model;

/**
 * Card Payment details
 */
public class CardPayment {

    private String nameOnCard;

    private String cardNumber;

    private String expDate;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

}
