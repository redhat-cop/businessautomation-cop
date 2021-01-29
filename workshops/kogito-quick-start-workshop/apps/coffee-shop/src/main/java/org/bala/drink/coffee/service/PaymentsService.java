package org.bala.drink.coffee.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.bala.drink.coffee.model.DrinkOrder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * This service invokes an external payments gateway (dummy) to process the payment
 */
@ApplicationScoped
public class PaymentsService {

    @Inject
    @RestClient
    PaymentsGateway paymentsGateway;

    public boolean process(DrinkOrder order) {
        // invoke an external payments gateway service
        Response response = paymentsGateway.makePayment(order.getCardPayment());
        System.out.println("*******************************************************");
        System.out.println("Payment processed successfully by the payment gateway");
        System.out.println("*******************************************************");
        return response.getStatus() == Response.Status.OK.getStatusCode();
    }
}
