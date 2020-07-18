    package org.redhat.services.model;

import java.io.Serializable;

@javax.persistence.Entity
@javax.xml.bind.annotation.XmlRootElement
@org.kie.api.definition.type.Label("Person")
public class Person extends org.drools.persistence.jpa.marshaller.VariableEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5611382618148268163L;

    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "PERSON_ID_GENERATOR")
    @javax.persistence.Id
    @javax.persistence.SequenceGenerator(name = "PERSON_ID_GENERATOR", sequenceName = "PERSON_ID_SEQ")
    @org.kie.api.definition.type.Label("Id")
    @org.kie.api.definition.type.Key
    private java.lang.Long id;

    @org.kie.api.definition.type.Label("Name")
    private String name;

    @org.kie.api.definition.type.Label("Surname")
    private String surname;

    @org.kie.api.definition.type.Label("Age")
    private Integer age;

    @org.kie.api.definition.type.Label("Underage")
    private Boolean underage;

    public java.lang.Long getId() {
        return id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean isUnderage() {
        return underage;
    }

    public void setUnderage(Boolean underage) {
        this.underage = underage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((age == null) ? 0 : age.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((underage == null) ? 0 : underage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (age == null) {
            if (other.age != null)
                return false;
        } else if (!age.equals(other.age))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", surname=" + surname + ", age=" + age + ", underage="
                + underage + "]";
    }

}
