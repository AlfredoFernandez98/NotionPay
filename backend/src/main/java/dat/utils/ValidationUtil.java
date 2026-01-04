package dat.utils;

import java.util.regex.Pattern;

public class ValidationUtil{

    // Email regex pattern

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValidEmail(String email){
       if(email == null || email.isEmpty() ){
           throw new NullPointerException("email cant be nul");
       }

      if(!EMAIL_PATTERN.matcher(email).matches()){
          throw new IllegalArgumentException("Invalid email address");
      }
        return true;
    }

    public static boolean isStrongPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        return true;
    }
    public static boolean isValidCompanyName(String companyName) {

        if (companyName == null){
            throw new NullPointerException("companyName cant be nul");
        }

        String trimmedCompanyName = companyName.trim();

        if (trimmedCompanyName.isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (trimmedCompanyName.length() >100){
            throw new IllegalArgumentException("Company name exceeds 100 characters");
        }

        return true;
    }
}
