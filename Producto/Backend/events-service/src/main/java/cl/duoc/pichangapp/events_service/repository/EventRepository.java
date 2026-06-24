package cl.duoc.pichangapp.events_service.repository;

import cl.duoc.pichangapp.events_service.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByStatusAndEventDateAfter(String status, LocalDateTime date);
    List<Event> findByStatusAndEventDateBefore(String status, LocalDateTime date);
    List<Event> findByOrganizerId(Integer organizerId);

    /**
     * Detección de duplicados: ¿el mismo organizador ya creó un evento con el mismo
     * nombre después de cierto instante (p. ej. hace 10 segundos)?
     */
    boolean existsByOrganizerIdAndNameAndCreatedAtAfter(Integer organizerId, String name, LocalDateTime after);
}
