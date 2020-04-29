package org.kie.kogito.app;

import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class CardPaymentMessageMarshaller implements MessageMarshaller<org.bala.drink.coffee.model.CardPayment> {

    public java.lang.Class<org.bala.drink.coffee.model.CardPayment> getJavaClass() {
        return org.bala.drink.coffee.model.CardPayment.class;
    }

    public String getTypeName() {
        return "org.kie.kogito.app.CardPayment";
    }

    public org.bala.drink.coffee.model.CardPayment readFrom(ProtoStreamReader reader) throws IOException {
        org.bala.drink.coffee.model.CardPayment value = new org.bala.drink.coffee.model.CardPayment();
        value.setCardNumber(reader.readString("cardNumber"));
        value.setExpDate(reader.readString("expDate"));
        value.setNameOnCard(reader.readString("nameOnCard"));
        return value;
    }

    public void writeTo(ProtoStreamWriter writer, org.bala.drink.coffee.model.CardPayment t) throws IOException {
        writer.writeString("cardNumber", t.getCardNumber());
        writer.writeString("expDate", t.getExpDate());
        writer.writeString("nameOnCard", t.getNameOnCard());
    }
}
