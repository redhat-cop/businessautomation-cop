package org.drools.bpmn2;

import java.util.Map;
import java.util.HashMap;

public class Barista_process_3_TaskOutput {

    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap();
        params.put("order_out", this.order_out);
        return params;
    }

    private org.bala.drink.coffee.model.DrinkOrder order_out;

    public org.bala.drink.coffee.model.DrinkOrder getOrder_out() {
        return order_out;
    }

    public void setOrder_out(org.bala.drink.coffee.model.DrinkOrder order_out) {
        this.order_out = order_out;
    }
}
// Task output model for user task 'Prepare Drink' in process 'barista_process'
