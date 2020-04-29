package org.drools.bpmn2;

import java.util.Map;
import java.util.HashMap;

@org.kie.internal.kogito.codegen.Generated(value = "kogit-codegen", reference = "drink_order_process", name = "Drink_order_process")
public class Drink_order_processModel implements org.kie.kogito.Model {

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap();
        params.put("process_state", this.process_state);
        params.put("order", this.order);
        return params;
    }

    @Override
    public void fromMap(Map<String, Object> params) {
        fromMap(null, params);
    }

    public void fromMap(String id, Map<String, Object> params) {
        this.id = id;
        this.process_state = (java.lang.Boolean) params.get("process_state");
        this.order = (org.bala.drink.coffee.model.DrinkOrder) params.get("order");
    }

    private java.lang.Boolean process_state;

    public java.lang.Boolean getProcess_state() {
        return process_state;
    }

    public void setProcess_state(java.lang.Boolean process_state) {
        this.process_state = process_state;
    }

    private org.bala.drink.coffee.model.DrinkOrder order;

    public org.bala.drink.coffee.model.DrinkOrder getOrder() {
        return order;
    }

    public void setOrder(org.bala.drink.coffee.model.DrinkOrder order) {
        this.order = order;
    }
}
