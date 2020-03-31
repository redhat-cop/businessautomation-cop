package com.mycompany.app;

import com.mycompany.app.models.Account;
import com.mycompany.app.models.User;

/**
 * Hello world!
 */
public class App {

	private final String message = "Hello World!";

	public App() {
	}

	public static void main(String[] args) {
		System.out.println(new App().getMessage());

		Account acc1 = new Account("11111", 0, 99);
		Account acc2 = new Account("22222",1, 999);
		Account acc3 = new Account("33333",2, 9999);
		User user1 = new User("Bill", "password123", "MANAGER");
		User user2 = new User("Betty", "password123", "SUPERVISOR");
		User user3 = new User("Bob", "password123", "USER");
		
		System.out.println("Account #1: "+acc1);
		System.out.println("Account #2: "+acc2);
		System.out.println("Account #3: "+acc3.toString());


	}

	private final String getMessage() {
		return message;
	}

}
