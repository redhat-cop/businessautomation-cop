package org.kie.kogito.persistence;

@javax.enterprise.context.ApplicationScoped()
public class KogitoProcessInstancesFactoryImpl extends org.kie.kogito.persistence.KogitoProcessInstancesFactory {

    public KogitoProcessInstancesFactoryImpl() {
        super(null);
    }

    @javax.inject.Inject()
    public KogitoProcessInstancesFactoryImpl(org.infinispan.client.hotrod.RemoteCacheManager param0) {
        super(param0);
    }

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "kogito.persistence.infinispan.template")
    java.util.Optional<java.lang.String> templateName;

    public String template() {
        return templateName.orElse("");
    }

    public String proto() {
        return "syntax = \"proto2\"; \npackage org.kie.kogito.app; \nimport \"kogito-types.proto\";\n\nmessage CardPayment { \n\toption java_package = \"org.bala.drink.coffee.model\";\n\toptional string cardNumber = 1; \n\toptional string expDate = 2; \n\toptional string nameOnCard = 3; \n}\nmessage DrinkOrder { \n\toption java_package = \"org.bala.drink.coffee.model\";\n\toptional CardPayment cardPayment = 1; \n\toptional string cupSize = 2; \n\toptional string drinkType = 3; \n\toptional double orderPrice = 4; \n}\n";
    }

    public java.util.List marshallers() {
        java.util.List list = new java.util.ArrayList();
        list.add(new org.kie.kogito.app.CardPaymentMessageMarshaller());
        list.add(new org.kie.kogito.app.DrinkOrderMessageMarshaller());
        return list;
    }
}
