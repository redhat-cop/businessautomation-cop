package org.redhat.appdev.enumloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class EnumLoader {
  
  private static final String JNDI = "java:jboss/datasources/EnumDS";
  private static Boolean TEST = false;

  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    TEST = true;
    (new org.redhat.appdev.enumloader.EnumLoader()).getProductCodes("India");
  }

  public EnumLoader() {

  }

  public List<String> getProductCodes(String region) throws SQLException {

    List<String> productCodes = new ArrayList<String>();

    Connection connection = null;
    Statement statement = null;
    try {
      if (TEST) {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/productcodes", "agiertli",
            "agiertli");
      } else {

        System.out.println("Creating Enum connection from JNDI:" + JNDI);

        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(JNDI);
        connection = ds.getConnection();
      }

      String selectTableSQL = String.format("SELECT name from productlist where region = '%s'", region);

      statement = connection.createStatement();

      System.out.println(selectTableSQL);

      // execute select SQL stetement
      ResultSet rs = statement.executeQuery(selectTableSQL);

      while (rs.next()) {

        String name = rs.getString("name");

        System.out.println("product name : " + name);
        productCodes.add(name);

      }

    } catch (Exception e) {
      e.printStackTrace();

    }
    return productCodes;
  }

}
