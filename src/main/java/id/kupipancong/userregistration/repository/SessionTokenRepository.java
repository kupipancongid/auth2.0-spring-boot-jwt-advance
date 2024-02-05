package id.kupipancong.userregistration.repository;

import id.kupipancong.userregistration.entity.Session;
import id.kupipancong.userregistration.entity.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, String> {
    Optional<SessionToken> findSessionTokenByAccessToken(String accessToken);

    Optional<SessionToken> findSessionTokenByRefreshToken(String refreshToken);

    @Modifying
    @Query("update SessionToken s set s.loggedOut = true where s.session = :session")
    void updateLoggedOutTrueBySession(Session session);
}

