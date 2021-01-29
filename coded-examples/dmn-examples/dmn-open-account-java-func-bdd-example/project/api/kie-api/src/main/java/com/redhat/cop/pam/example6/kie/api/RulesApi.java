package com.redhat.cop.pam.example6.kie.api;

import com.redhat.cop.pam.example6.CanOpenAccountResults;
import com.redhat.cop.pam.example6.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
