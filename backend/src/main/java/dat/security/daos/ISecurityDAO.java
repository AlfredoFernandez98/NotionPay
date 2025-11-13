package dat.security.daos;

import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;

public interface ISecurityDAO {
    UserDTO getVerifiedUser(String email, String password) throws ValidationException;
    User createUser(String email, String password);
    User addRole(UserDTO user, String newRole);

    User getUserByEmail(String email);
}
