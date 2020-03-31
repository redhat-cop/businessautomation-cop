package com.mycompany.app.models;

public class Account {
	private String accountNum;
	private int accountLevel;
	private int balance;

	public Account(String accountNum, int accountLevel, int balance) {
		super();
		this.accountNum = accountNum;
		this.accountLevel = accountLevel;
		this.balance = balance;
	}

	public String getAccountNum() {
		return accountNum;
	}

	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}

	public int getAccountLevel() {
		return accountLevel;
	}

	public void setAccountLevel(int accountLevel) {
		this.accountLevel = accountLevel;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "acc no: " + accountNum + " - balance: " + balance + " - type: " + accountLevel ;
	}

}
