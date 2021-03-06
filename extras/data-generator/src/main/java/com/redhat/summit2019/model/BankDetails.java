package com.redhat.summit2019.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBankDetails.class)
@JsonDeserialize(as = ImmutableBankDetails.class)
@JsonTypeName("BankDetails")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public abstract class BankDetails {
    public abstract long accountNumber();
    public abstract String name();
    public abstract long sortCode();
}
