package cl.duoc.pichangapp.events_service.scheduler;

import cl.duoc.pichangapp.events_service.model.Event;
import cl.duoc.pichangapp.events_service.model.EventRegistration;
import cl.duoc.pichangapp.events_service.repository.EventRegistrationRepository;
import cl.duoc.pichangapp.events_service.repository.EventRepository;
import cl.duoc.pichangapp.events_service.service.KarmaServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final KarmaServiceClient karmaServiceClient;

    @Scheduled(fixedRate = 1800000) // 30 minutes
    @Transactional
    public void processFinishedEvents() {
        log.info("Running job to find and finish old events");
        LocalDateTime fourHoursAgo = LocalDateTime.now().minusHours(4);
        List<Event> oldEvents = eventRepository.findByStatusAndEventDateBefore("ACTIVO", fourHoursAgo);

        for (Event event : oldEvents) {
            log.info("Auto-finishing event {}", event.getId());
            event.setStatus("FINALIZADO");
            event.setFinishedAt(LocalDateTime.now());
            eventRepository.save(event);

            List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(event.getId());
            for (EventRegistration reg : registrations) {
                if ("REGISTERED".equals(reg.getStatus())) {
                    reg.setStatus("ABSENT");
                    eventRegistrationRepository.save(reg);
                    karmaServiceClient.registerAbsence(reg.getUserId(), event.getId());
                }
            }
        }
    }
}
