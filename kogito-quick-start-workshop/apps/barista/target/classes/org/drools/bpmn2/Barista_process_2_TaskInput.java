package org.drools.bpmn2;

import java.util.Map;

public class Barista_process_2_TaskInput {

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

    public static Barista_process_2_TaskInput fromMap(String id, String name, Map<String, Object> params) {
        Barista_process_2_TaskInput item = new Barista_process_2_TaskInput();
        item._id = id;
        item._name = name;
        item.order = (org.bala.drink.coffee.model.DrinkOrder) params.get("order");
        return item;
    }

    private org.bala.drink.coffee.model.DrinkOrder order;

    public org.bala.drink.coffee.model.DrinkOrder getOrder() {
        return order;
    }

    public void setOrder(org.bala.drink.coffee.model.DrinkOrder order) {
        this.order = order;
    }
}
// Task input model for user task 'Collect Drink' in process 'barista_process'
