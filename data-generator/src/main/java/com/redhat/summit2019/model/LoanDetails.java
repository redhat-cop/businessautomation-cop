package com.redhat.summit2019.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLoanDetails.class)
@JsonDeserialize(as = ImmutableLoanDetails.class)
public abstract class LoanDetails {
    public abstract Person person();
    public abstract Farm farm();
    public abstract Location location();
    public abstract BankDetails bankDetails();
    public abstract Loan loan();
}
