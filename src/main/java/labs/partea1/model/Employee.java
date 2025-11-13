package labs.partea1.model;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Employee {
    private int id;
    private String name;

    // Constructor gol obligatoriu pentru JAXB (XML)
    public Employee() {
    }

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
