package com.bacop.jwt_dm_project;

public class Country {

  String country;

  public Country() {
    super();
  }

  public Country(String country) {
    super();
    this.country = country;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Country [country=").append(country).append("]");
    return builder.toString();
  }



}
