package id.kupipancong.userregistration.repository;

import id.kupipancong.userregistration.entity.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, String> {
    Optional<SessionToken> findSessionTokenByAccessToken(String accessToken);
}

