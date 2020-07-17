package com.redhat.summit2019.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFarm.class)
@JsonDeserialize(as = ImmutableFarm.class)
@JsonTypeName("Farm")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public abstract class Farm {
    public abstract String name();
    public abstract String type();
    public abstract int size();

    @Override
    public String toString() {
        return name() + " " + type();
    }
}
