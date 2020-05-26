package com.redhat.cop.pam.example4.kie.api;

import com.redhat.cop.pam.example4.CanOpenAccountResults;
import com.redhat.cop.pam.example4.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
