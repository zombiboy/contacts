package wvw.mobile.rules.dto;

import java.io.Serializable;

public class Contact implements Serializable {
    String id;
    String name;
    String phone;
    String prenom;
    String email;
    String birthday;
    String sexe;

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.prenom="";
        this.email="";
        this.birthday="";
        this.sexe="";
    }

    public Contact() {
        this.prenom="";
        this.email="";
        this.birthday="";
        this.sexe="";
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }
}