package cl.duoc.pichangapp.karma_service.repository;

import cl.duoc.pichangapp.karma_service.model.KarmaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KarmaHistoryRepository extends JpaRepository<KarmaHistory, Integer> {
    List<KarmaHistory> findByKarmaScoreId(Integer karmaScoreId);
}
