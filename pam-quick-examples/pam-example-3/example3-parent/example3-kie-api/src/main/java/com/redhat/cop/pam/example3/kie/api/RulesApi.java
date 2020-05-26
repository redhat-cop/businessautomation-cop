package com.redhat.cop.pam.example3.kie.api;

import com.redhat.cop.pam.example3.CanOpenAccountResults;
import com.redhat.cop.pam.example3.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
