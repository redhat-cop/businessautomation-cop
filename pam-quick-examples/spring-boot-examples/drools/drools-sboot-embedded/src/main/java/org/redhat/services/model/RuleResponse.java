package org.redhat.services.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RuleResponse implements Serializable {

    private int rulesFired;
    private String executionReference;
    private Object payload;

}