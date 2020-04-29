package org.drools.bpmn2;

import java.util.Map;

public class Barista_process_3_TaskInput {

    private String _id;

    private String _name;

    public void setId(String id) {
        this._id = id;
    }

    public String getId() {
        return this._id;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getName() {
        return this._name;
    }

    public static Barista_process_3_TaskInput fromMap(String id, String name, Map<String, Object> params) {
        Barista_process_3_TaskInput item = new Barista_process_3_TaskInput();
        item._id = id;
        item._name = name;
        item.order_in = (org.bala.drink.coffee.model.DrinkOrder) params.get("order_in");
        return item;
    }

    private org.bala.drink.coffee.model.DrinkOrder order_in;

    public org.bala.drink.coffee.model.DrinkOrder getOrder_in() {
        return order_in;
    }

    public void setOrder_in(org.bala.drink.coffee.model.DrinkOrder order_in) {
        this.order_in = order_in;
    }
}
// Task input model for user task 'Prepare Drink' in process 'barista_process'
