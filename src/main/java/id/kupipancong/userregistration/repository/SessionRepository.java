package id.kupipancong.userregistration.repository;

import id.kupipancong.userregistration.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findSessionByUserId(String userId);
    List<Session> getAllByUserId(String userId);
}
