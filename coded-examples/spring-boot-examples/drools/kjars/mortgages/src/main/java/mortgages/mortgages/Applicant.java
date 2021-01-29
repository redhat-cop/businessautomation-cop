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

public class Applicant extends java.lang.Object implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    @org.kie.api.definition.type.Position(value = 0)
    private java.lang.Integer age;

    @org.kie.api.definition.type.Position(value = 1)
    private java.util.Date applicationDate;

    @org.kie.api.definition.type.Position(value = 4)
    private java.lang.Boolean approved;

    @org.kie.api.definition.type.Position(value = 2)
    private java.lang.String creditRating;

    @org.kie.api.definition.type.Position(value = 3)
    private java.lang.String name;

    public Applicant() {
    }

    public Applicant( java.lang.Integer age, java.util.Date applicationDate, java.lang.String creditRating, java.lang.String name, java.lang.Boolean approved ) {
        this.age = age;
        this.applicationDate = applicationDate;
        this.creditRating = creditRating;
        this.name = name;
        this.approved = approved;
    }

    public java.lang.Integer getAge() {
        return this.age;
    }

    public void setAge( java.lang.Integer age ) {
        this.age = age;
    }

    public java.util.Date getApplicationDate() {
        return this.applicationDate;
    }

    public void setApplicationDate( java.util.Date applicationDate ) {
        this.applicationDate = applicationDate;
    }

    public java.lang.Boolean getApproved() {
        return this.approved;
    }

    public void setApproved( java.lang.Boolean approved ) {
        this.approved = approved;
    }

    public java.lang.String getCreditRating() {
        return this.creditRating;
    }

    public void setCreditRating( java.lang.String creditRating ) {
        this.creditRating = creditRating;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName( java.lang.String name ) {
        this.name = name;
    }
}
