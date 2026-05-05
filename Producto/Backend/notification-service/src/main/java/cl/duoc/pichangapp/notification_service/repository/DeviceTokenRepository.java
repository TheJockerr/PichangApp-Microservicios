package cl.duoc.pichangapp.notification_service.repository;

import cl.duoc.pichangapp.notification_service.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {
    List<DeviceToken> findByUserId(String userId);
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
