package com.redhat.cop.pam.example5.kie.api;

import com.redhat.cop.pam.example5.CanOpenAccountResults;
import com.redhat.cop.pam.example5.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
