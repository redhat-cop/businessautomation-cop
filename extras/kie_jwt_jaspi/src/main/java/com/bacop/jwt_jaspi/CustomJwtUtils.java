/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bacop.jwt_jaspi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author erouvas
 */
public class CustomJwtUtils {

  private static final Logger LOG = Logger.getLogger(CustomJwtUtils.class.getName());

  static final String MECHANISM_NAME = "JWT_JASPI";

  public boolean isBlank(String str) {
    return (str == null) || (str.isEmpty()) || (str.length() == 0);
  }

  /**
   *
   * @param conn
   *
   * @return
   *
   * @throws SQLException
   */
  public boolean hasDynaRoles(Connection conn) throws SQLException {

    String tName = "";
    String sql1 = "show tables";
    PreparedStatement ps = conn.prepareStatement(sql1);
    ResultSet rs = ps.executeQuery();
//              ResultSetMetaData md = rs.getMetaData();
//              int colcount = md.getColumnCount();
//              LOG.log(Level.INFO, "CUSTOM_JWT H2 colcount:{0}", colcount);
//              for (int i=0; i<colcount; i++) {
//                LOG.log(Level.INFO, "CUSTOM_JWT H2 md.getColumnName:{0}", md.getColumnName(i+1));
//              }
    while (rs.next()) {
      tName = rs.getString("TABLE_NAME");
      LOG.log(Level.INFO, "CUSTOM_JWT H2 TABLE_NAME:{0}", tName);
    }
    rs.close();
    ps.close();

    return tName.equalsIgnoreCase("dynaroles");
  }

  /**
   *
   * @param conn
   *
   * @throws SQLException
   */
  public void createDynaRoles(Connection conn) throws SQLException {
    String sql = "create table dynaroles (roleId bigint auto_increment primary key, uuid varchar(250), username varchar(250), rolename varchar(250) )";
    Statement st = conn.createStatement();
    st.executeUpdate(sql);
    st.close();
  }

  /**
   *
   * @param conn
   * @param user
   * @param role
   *
   * @throws SQLException
   */
  public void insertDynaRoles(Connection conn, int uuid, String user, String role) throws SQLException {
    String sql = "insert into dynaroles (uuid, username,rolename) values (?, ?,?)";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, Integer.toString(uuid));
    ps.setString(2, user);
    ps.setString(3, role);
    ps.execute();
    ps.close();
  }

  /**
   *
   * @param conn
   * @param user
   *
   * @return
   *
   * @throws SQLException
   */
  public ArrayList<String> readDynaRoles(Connection conn, String user) throws SQLException {
    ArrayList<String> dynaList = new ArrayList<>();
    PreparedStatement ps = conn.prepareStatement("select rolename from dynaroles where username=?");
    ps.setString(1, user);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
      dynaList.add(rs.getString(1));
    }
    rs.close();
    ps.close();
    return dynaList;
  }

  /**
   *
   * @param conn
   * @param uuid
   *
   * @return
   *
   * @throws SQLException
   */
  public ArrayList<String> deleteDynaRoles(Connection conn, int uuid) throws SQLException {
    ArrayList<String> dynaList = new ArrayList<>();
    PreparedStatement ps = conn.prepareStatement("delete from dynaroles where uuid=?");
    ps.setString(1, Integer.toString(uuid));
    ps.executeUpdate();
    ps.close();
    return dynaList;
  }

}
