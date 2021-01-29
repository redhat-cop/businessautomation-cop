/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mortgages.mortgages;

public class LoanApplication extends java.lang.Object implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    @org.kie.api.definition.type.Position(value = 0)
    private java.lang.Integer amount;

    @org.kie.api.definition.type.Position(value = 1)
    private java.lang.Boolean approved;

    @org.kie.api.definition.type.Position(value = 3)
    private java.lang.Integer approvedRate;

    @org.kie.api.definition.type.Position(value = 2)
    private java.lang.Integer deposit;

    @org.kie.api.definition.type.Position(value = 5)
    private java.lang.String explanation;

    @org.kie.api.definition.type.Position(value = 6)
    private java.lang.Integer insuranceCost;

    @org.kie.api.definition.type.Position(value = 4)
    private java.lang.Integer lengthYears;

    public LoanApplication() {
    }

    public LoanApplication( java.lang.Integer amount, java.lang.Boolean approved, java.lang.Integer deposit, java.lang.Integer approvedRate, java.lang.Integer lengthYears, java.lang.String explanation, java.lang.Integer insuranceCost ) {
        this.amount = amount;
        this.approved = approved;
        this.deposit = deposit;
        this.approvedRate = approvedRate;
        this.lengthYears = lengthYears;
        this.explanation = explanation;
        this.insuranceCost = insuranceCost;
    }

    public java.lang.Integer getAmount() {
        return this.amount;
    }

    public void setAmount( java.lang.Integer amount ) {
        this.amount = amount;
    }

    public java.lang.Boolean getApproved() {
        return this.approved;
    }

    public void setApproved( java.lang.Boolean approved ) {
        this.approved = approved;
    }

    public java.lang.Integer getApprovedRate() {
        return this.approvedRate;
    }

    public void setApprovedRate( java.lang.Integer approvedRate ) {
        this.approvedRate = approvedRate;
    }

    public java.lang.Integer getDeposit() {
        return this.deposit;
    }

    public void setDeposit( java.lang.Integer deposit ) {
        this.deposit = deposit;
    }

    public java.lang.String getExplanation() {
        return this.explanation;
    }

    public void setExplanation( java.lang.String explanation ) {
        this.explanation = explanation;
    }

    public java.lang.Integer getInsuranceCost() {
        return this.insuranceCost;
    }

    public void setInsuranceCost( java.lang.Integer insuranceCost ) {
        this.insuranceCost = insuranceCost;
    }

    public java.lang.Integer getLengthYears() {
        return this.lengthYears;
    }

    public void setLengthYears( java.lang.Integer lengthYears ) {
        this.lengthYears = lengthYears;
    }
}
