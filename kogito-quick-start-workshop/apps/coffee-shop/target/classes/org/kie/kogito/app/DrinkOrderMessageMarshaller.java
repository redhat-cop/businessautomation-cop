package org.kie.kogito.app;

import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class DrinkOrderMessageMarshaller implements MessageMarshaller<org.bala.drink.coffee.model.DrinkOrder> {

    public java.lang.Class<org.bala.drink.coffee.model.DrinkOrder> getJavaClass() {
        return org.bala.drink.coffee.model.DrinkOrder.class;
    }

    public String getTypeName() {
        return "org.kie.kogito.app.DrinkOrder";
    }

    public org.bala.drink.coffee.model.DrinkOrder readFrom(ProtoStreamReader reader) throws IOException {
        org.bala.drink.coffee.model.DrinkOrder value = new org.bala.drink.coffee.model.DrinkOrder();
        value.setCardPayment(reader.readObject("cardPayment", org.bala.drink.coffee.model.CardPayment.class));
        value.setCupSize(reader.readString("cupSize"));
        value.setDrinkType(reader.readString("drinkType"));
        value.setOrderPrice(reader.readDouble("orderPrice"));
        return value;
    }

    public void writeTo(ProtoStreamWriter writer, org.bala.drink.coffee.model.DrinkOrder t) throws IOException {
        writer.writeObject("cardPayment", t.getCardPayment(), org.bala.drink.coffee.model.CardPayment.class);
        writer.writeString("cupSize", t.getCupSize());
        writer.writeString("drinkType", t.getDrinkType());
        writer.writeDouble("orderPrice", t.getOrderPrice());
    }
}
