package com.bacop.jwt_dm_project.javaClient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

  private String fancyPrefix = "[" + this.getClass().getPackage().getName() + "] ..::|| ";

  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public AppTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(AppTest.class);
  }

  /**
   * Rigorous Test :-)
   */
  public void testApp() {

    assertTrue(true);
  }

}
