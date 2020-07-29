package org.redhat.services.model;

import java.io.Serializable;

public class Person implements Serializable {

	private static final long serialVersionUID = -5215086372987437096L;
	private String name;

	public Person(String name) {
		this.name = name;
	}

	public Person() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + "]";
	}

}
