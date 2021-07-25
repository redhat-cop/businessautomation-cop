package com.example.simple_rule;

/**
 * ContinentMap: map Countries to Continents
 */
public class ContinentMap {

  /**
   * The name of the continent
   */
  String continent;

  /**
   * name of country
   */
  String country;

  public ContinentMap() {
    super();
  }

  public ContinentMap(String continent, String country) {
    super();
    this.continent = continent;
    this.country = country;
  }

  /**
   * @return the continent
   */
  public String getContinent() {
    return continent;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param continent the continent to set
   */
  public void setContinent(String continent) {
    this.continent = continent;
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
    builder.append("ContinentMap [continent=").append(continent).append(", country=").append(country).append("]");
    return builder.toString();
  }

}
