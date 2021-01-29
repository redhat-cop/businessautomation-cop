package com.redhat.summit2019.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLocation.class)
@JsonDeserialize(as = ImmutableLocation.class)
@JsonTypeName("Location")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public abstract class Location {
    @Value.Parameter
    public abstract String name();
    @Value.Parameter
    public abstract String council();

    @Override
    public String toString() {
        return name() + ", " + council();
    }
}
