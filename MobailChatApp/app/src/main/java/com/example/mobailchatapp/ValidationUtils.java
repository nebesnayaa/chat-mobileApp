package com.example.mobailchatapp;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && email.endsWith("@gmail.com");
    }

    public static boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 4 &&
                password.length() <= 16 &&
                !password.contains(" ");
    }

    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }
}
