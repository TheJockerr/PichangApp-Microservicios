package cl.duoc.pichangapp.karma_service.repository;

import cl.duoc.pichangapp.karma_service.model.KarmaScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KarmaScoreRepository extends JpaRepository<KarmaScore, Integer> {
    Optional<KarmaScore> findByUserId(String userId);
}
