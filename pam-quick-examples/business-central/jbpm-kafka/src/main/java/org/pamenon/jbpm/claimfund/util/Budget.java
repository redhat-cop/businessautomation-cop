package org.pamenon.jbpm.claimfund.util;

public class Budget {
	
	private static Budget budgetInstance = null;
	
	public long total;
	
	private Budget() {
		// 1 Million
		total = 1000000;
	}
	
	synchronized public static Budget getInstance() {
		if(budgetInstance == null) {
			budgetInstance = new Budget();
		}
		
		return budgetInstance;
	}

}
