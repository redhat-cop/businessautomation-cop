package com.redhat.cop.pam.example4;

import org.kie.api.remote.Remotable;
import org.kie.dmn.feel.lang.FEELProperty;
import org.kie.dmn.feel.lang.FEELType;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

@FEELType
@Remotable
@XmlType(name="Customer")
public class Customer implements Serializable {

    private String name;

    private String surname;

    private String dateOfBirth;

    @FEELProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @FEELProperty("surname")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @FEELProperty("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(getName(), customer.getName()) &&
                Objects.equals(getSurname(), customer.getSurname()) &&
                Objects.equals(getDateOfBirth(), customer.getDateOfBirth());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSurname(), getDateOfBirth());
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
