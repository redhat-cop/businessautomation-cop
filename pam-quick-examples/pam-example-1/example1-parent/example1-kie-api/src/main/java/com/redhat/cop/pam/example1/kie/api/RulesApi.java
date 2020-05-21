package com.redhat.cop.pam.example1.kie.api;

import com.redhat.cop.pam.example1.CanOpenAccountResults;
import com.redhat.cop.pam.example1.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
