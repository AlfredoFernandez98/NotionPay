package dat.security.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Set;

/**
 * Purpose: Data Transfer Object for User
 * Author: NotionPay Team
 * 
 * Security Note: Password is excluded from toString(), equals(), and hashCode()
 * to prevent accidental exposure in logs or debugging output.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String email;
    
    /**
     * Password field - WRITE_ONLY for security
     * Can be received in requests but never sent in responses
     * WARNING: Never log this field or include it in API responses
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Allows reading from JSON but never writing to JSON
    private String password;
    
    private Set<String> roles;

    /**
     * Constructor for user with email and roles (no password)
     * Used when returning user info without sensitive data
     */
    public UserDTO(String email, Set<String> roles) {
        this.email = email;
        this.roles = roles;
        this.password = null; // Explicitly set to null for clarity
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "email='" + email + '\'' +
                ", roles=" + roles +
                ", password=[PROTECTED]" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return email != null ? email.equals(userDTO.email) : userDTO.email == null;
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}

