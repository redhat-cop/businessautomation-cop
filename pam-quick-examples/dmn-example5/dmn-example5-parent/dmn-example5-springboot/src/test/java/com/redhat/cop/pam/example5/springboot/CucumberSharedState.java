package com.redhat.cop.pam.example5.springboot;

import com.redhat.cop.pam.example5.CanOpenAccountResults;
import com.redhat.cop.pam.example5.Customer;

import org.springframework.stereotype.Component;

@Component
public class CucumberSharedState {

    private Customer customer;

    private CanOpenAccountResults canOpenAccountResults;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public CanOpenAccountResults getCanOpenAccountResults() {
        return canOpenAccountResults;
    }

    public void setCanOpenAccountResults(CanOpenAccountResults canOpenAccountResults) {
        this.canOpenAccountResults = canOpenAccountResults;
    }

}