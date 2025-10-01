package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class User {
    private String name;
    private String email;
    private String age;

    public User() {}
    public User(String name, String email, String age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getAge()   { return age; }
    public void setName(String n)  { this.name = n; }
    public void setEmail(String e) { this.email = e; }
    public void setAge(String a)   { this.age = a; }
}

