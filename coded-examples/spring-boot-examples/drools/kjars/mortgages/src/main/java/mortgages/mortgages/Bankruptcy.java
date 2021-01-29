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

public class Bankruptcy extends java.lang.Object implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    @org.kie.api.definition.type.Position(value = 0)
    private java.lang.Integer amountOwed;

    @org.kie.api.definition.type.Position(value = 1)
    private java.lang.Integer yearOfOccurrence;

    public Bankruptcy() {
    }

    public Bankruptcy( java.lang.Integer amountOwed, java.lang.Integer yearOfOccurrence ) {
        this.amountOwed = amountOwed;
        this.yearOfOccurrence = yearOfOccurrence;
    }

    public java.lang.Integer getAmountOwed() {
        return this.amountOwed;
    }

    public void setAmountOwed( java.lang.Integer amountOwed ) {
        this.amountOwed = amountOwed;
    }

    public java.lang.Integer getYearOfOccurrence() {
        return this.yearOfOccurrence;
    }

    public void setYearOfOccurrence( java.lang.Integer yearOfOccurrence ) {
        this.yearOfOccurrence = yearOfOccurrence;
    }
}
