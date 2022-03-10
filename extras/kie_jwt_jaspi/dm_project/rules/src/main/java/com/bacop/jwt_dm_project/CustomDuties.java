package com.bacop.jwt_dm_project;

public class CustomDuties {

    String continent;
    String country;
    String apply;


    public CustomDuties() {
    }

    public CustomDuties(String continent, String country, String apply) {
        this.continent = continent;
        this.country = country;
        this.apply = apply;
    }

    public String getContinent() {
        return this.continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getApply() {
        return this.apply;
    }

    public void setApply(String apply) {
        this.apply = apply;
    }

    public CustomDuties continent(String continent) {
        setContinent(continent);
        return this;
    }

    public CustomDuties country(String country) {
        setCountry(country);
        return this;
    }

    public CustomDuties apply(String apply) {
        setApply(apply);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " continent='" + getContinent() + "'" +
            ", country='" + getCountry() + "'" +
            ", apply='" + getApply() + "'" +
            "}";
    }

}
