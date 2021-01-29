package com.mycompany.app.models;

public class User {
	private String userName;
	private String password;
	private String userType;
	
	public User(String userName, String password, String userLevel) {
		super();
		this.userName = userName;
		this.password = password;
		this.userType = userLevel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userLevel) {
		this.userType = userLevel;
	}
	
	@Override
	public String toString() {
		return this.userName + " -- " + this.password + " -- " + this.userType;
	}
	

}