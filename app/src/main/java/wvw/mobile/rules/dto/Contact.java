package wvw.mobile.rules.dto;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import wvw.mobile.rules.util.ColorUtils;

public class Contact implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String phone2;
    private String phone3;
    private String prenom;
    private String email;
    private String birthday;
    private String sexe;
    //Utiliser dans la metode show pour afficher le liens avec le contact actuel
    private String relationFind;
    private List<Contact> contactsLiens=null;
    private int backgroundColor = Color.BLUE;

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.prenom="";
        this.email="";
        this.birthday="";
        this.sexe="";
        this.backgroundColor = ColorUtils.getRandomMaterialColor();
    }

    public Contact() {
        this.prenom="";
        this.email="";
        this.birthday="";
        this.sexe="";
        this.backgroundColor = ColorUtils.getRandomMaterialColor();
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

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone3() {
        return phone3;
    }

    public void setPhone3(String phone3) {
        this.phone3 = phone3;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public List<Contact> getContactsLiens() {
        return contactsLiens;
    }

    public void setContactsLiens(List<Contact> contactsLiens) {
        this.contactsLiens = contactsLiens;
    }

    public String getRelationFind() {
        return relationFind;
    }

    public void setRelationFind(String relationFind) {
        this.relationFind = relationFind;
    }

    /*
     * Comparator pour le tri des contacts par leurs noms
     */
    public static Comparator<Contact> ComparatorName = new Comparator<Contact>() {

        @Override
        public int compare(Contact c1, Contact c2) {
            return c1.getName().compareTo(c2.getName());
        }
    };
}