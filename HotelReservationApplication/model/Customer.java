package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Customer {
    private String firstName;
    private String lastName;
    private String email;

    public Customer(String firstName, String lastName, String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException();
        } else {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + "(" + email + ")";
    }

    private boolean isValidEmail(String emailStr) {
        String emailRegex = "^(.+)@(.+).com$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(emailStr);
        return matcher.matches();
    }

}
