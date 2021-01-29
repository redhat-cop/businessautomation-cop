package com.redhat.cop.pam.example2.kie.api;

import com.redhat.cop.pam.example2.CanOpenAccountResults;
import com.redhat.cop.pam.example2.Customer;

public interface RulesApi {
    public CanOpenAccountResults canOpenAccount(final Customer customer);
}
