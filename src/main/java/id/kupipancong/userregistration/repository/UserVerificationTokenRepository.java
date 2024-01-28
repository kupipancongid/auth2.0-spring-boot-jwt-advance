package id.kupipancong.userregistration.repository;


import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.entity.UserVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerificationTokenRepository extends JpaRepository<UserVerificationToken, String> {
    Optional<UserVerificationToken> findUserVerificationTokenByToken(String token);
    Optional<UserVerificationToken> findByUser(User user);
}
