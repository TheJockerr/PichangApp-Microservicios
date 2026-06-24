package cl.duoc.pichangapp.events_service.service;

import cl.duoc.pichangapp.events_service.dto.AdminEventDTO;
import cl.duoc.pichangapp.events_service.dto.CreateEventRequest;
import cl.duoc.pichangapp.events_service.dto.EventRegistrationDTO;
import cl.duoc.pichangapp.events_service.dto.EventResponseDTO;
import cl.duoc.pichangapp.events_service.exception.EventNotFoundException;
import cl.duoc.pichangapp.events_service.model.Event;
import cl.duoc.pichangapp.events_service.model.EventRegistration;
import cl.duoc.pichangapp.events_service.repository.EventRegistrationRepository;
import cl.duoc.pichangapp.events_service.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final KarmaServiceClient karmaServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final UsersServiceClient usersServiceClient;

    @Transactional
    public EventResponseDTO createEvent(CreateEventRequest request, Integer organizerId) {
        // Idempotencia: rechaza el mismo organizador + nombre creado en los últimos 10 segundos.
        LocalDateTime diezSegundosAtras = LocalDateTime.now().minusSeconds(10);
        if (eventRepository.existsByOrganizerIdAndNameAndCreatedAtAfter(
                organizerId, request.getName(), diezSegundosAtras)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Evento duplicado. Por favor espera antes de crear otro.");
        }

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setName(request.getName());
        event.setSport(request.getSport());
        event.setEventDate(request.getEventDate());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setLocationName(request.getLocationName());
        event.setMaxPlayers(request.getMaxPlayers());
        event.setCurrentPlayers(0);
        event.setStatus("ACTIVO");
        event.setCreatedAt(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        return mapToDTO(saved, null);
    }

    public List<EventResponseDTO> findNearbyEvents(double lat, double lng) {
        LocalDateTime now = LocalDateTime.now();
        List<Event> activeEvents = eventRepository.findByStatusAndEventDateAfter("ACTIVO", now);
        java.util.Map<Integer, String> nameCache = new java.util.HashMap<>();

        return activeEvents.stream()
                .map(event -> {
                    double distance = calculateDistance(lat, lng, event.getLatitude(), event.getLongitude());
                    return mapToDTO(event, distance, nameCache);
                })
                .sorted(Comparator.comparing(EventResponseDTO::getDistanceKm))
                .collect(Collectors.toList());
    }

    public EventResponseDTO getEventDetails(Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        return mapToDTO(event, null);
    }

    @Transactional
    public void joinEvent(Integer eventId, Integer userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (!"ACTIVO".equals(event.getStatus())) {
            throw new IllegalStateException("Event is not active");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot join a past event");
        }
        if (event.getOrganizerId().equals(userId)) {
            throw new IllegalStateException("Organizer cannot join their own event as a participant");
        }
        if (event.getCurrentPlayers() >= event.getMaxPlayers()) {
            throw new IllegalStateException("Event is full");
        }
        if (eventRegistrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new IllegalStateException("User already registered");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(userId);
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        eventRegistrationRepository.save(registration);

        event.setCurrentPlayers(event.getCurrentPlayers() + 1);
        eventRepository.save(event);
    }

    @Transactional
    public void leaveEvent(Integer eventId, Integer userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        EventRegistration registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalStateException("User is not registered for this event"));

        if (!event.getEventDate().minusHours(2).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("No puedes cancelar con menos de 2 horas de anticipación");
        }

        eventRegistrationRepository.delete(registration);
        event.setCurrentPlayers(event.getCurrentPlayers() - 1);
        eventRepository.save(event);
    }

    public List<EventRegistrationDTO> getEventRegistrations(Integer eventId, Integer organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Only the organizer can view registrations");
        }
        return eventRegistrationRepository.findByEventId(eventId).stream()
                .map(this::mapRegToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String markAttendance(Integer eventId, Integer organizerId, Integer userId, boolean attended) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento no encontrado"));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Solo el organizador puede marcar asistencia");
        }

        if (LocalDateTime.now().isBefore(event.getEventDate().plusMinutes(5))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No puedes registrar asistencia hasta 5 minutos después del inicio del evento"
            );
        }

        EventRegistration registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario no está inscrito en este evento"));

        if (!"REGISTERED".equals(registration.getStatus())) {
            throw new IllegalStateException("La asistencia ya fue marcada");
        }

        if (attended) {
            registration.setStatus("ATTENDED");
            karmaServiceClient.registerCheckIn(userId, eventId);
        } else {
            registration.setStatus("ABSENT");
            karmaServiceClient.registerAbsence(userId, eventId);
        }
        eventRegistrationRepository.save(registration);
        return "Asistencia registrada correctamente";
    }

    @Transactional
    public void finishEvent(Integer eventId, Integer organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento no encontrado"));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Solo el organizador puede finalizar el evento");
        }

        if (LocalDateTime.now().isBefore(event.getEventDate().plusMinutes(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes finalizar el evento hasta 5 minutos después de su hora de inicio");
        }

        event.setStatus("FINALIZADO");
        event.setFinishedAt(LocalDateTime.now());
        eventRepository.save(event);

        List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(eventId);
        for (EventRegistration reg : registrations) {
            if ("REGISTERED".equals(reg.getStatus())) {
                reg.setStatus("ABSENT");
                eventRegistrationRepository.save(reg);
                karmaServiceClient.registerAbsence(reg.getUserId(), eventId);
            }
        }
    }

    public List<EventResponseDTO> getMyEvents(Integer userId) {
        List<EventRegistration> registrations = eventRegistrationRepository.findByUserId(userId);
        java.util.Map<Integer, String> nameCache = new java.util.HashMap<>();
        return registrations.stream()
                .map(reg -> eventRepository.findById(reg.getEventId()).orElse(null))
                .filter(event -> event != null)
                .map(event -> mapToDTO(event, null, nameCache))
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getOrganizingEvents(Integer userId) {
        java.util.Map<Integer, String> nameCache = new java.util.HashMap<>();
        return eventRepository.findByOrganizerId(userId).stream()
                .filter(event -> "ACTIVO".equals(event.getStatus()))
                .map(event -> mapToDTO(event, null, nameCache))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEvent(Integer eventId, Integer organizerId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el organizador puede eliminar el evento");
        }

        cancelEventAndCompensate(event);
    }

    /**
     * Eliminación de evento por un administrador.
     * Mismo flujo que la eliminación por organizador (karma + notificación) pero
     * sin la restricción de que quien elimina sea el organizador.
     */
    @Transactional
    public void deleteEventAsAdmin(Integer eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        cancelEventAndCompensate(event);
    }

    /**
     * Borrado de cuenta: cancela todos los eventos ACTIVO que organiza el usuario
     * (mismo flujo de compensación que DELETE /{id}) y elimina sus inscripciones como
     * participante en otros eventos. Lo invoca users-service con token interno.
     */
    @Transactional
    public void deleteEventsByUser(Integer userId) {
        // 1. Cancelar los eventos ACTIVO organizados por el usuario.
        List<Event> organizados = eventRepository.findByOrganizerId(userId).stream()
                .filter(e -> "ACTIVO".equals(e.getStatus()))
                .collect(Collectors.toList());
        for (Event e : organizados) {
            cancelEventAndCompensate(e);
        }

        // 2. Eliminar las inscripciones del usuario como participante (ajustando cupos).
        List<EventRegistration> inscripciones = eventRegistrationRepository.findByUserId(userId);
        for (EventRegistration reg : inscripciones) {
            eventRepository.findById(reg.getEventId()).ifPresent(ev -> {
                if ("ACTIVO".equals(ev.getStatus()) && ev.getCurrentPlayers() != null && ev.getCurrentPlayers() > 0) {
                    ev.setCurrentPlayers(ev.getCurrentPlayers() - 1);
                    eventRepository.save(ev);
                }
            });
            eventRegistrationRepository.delete(reg);
        }
    }

    /**
     * Lista todos los eventos (activos, finalizados y cancelados) para el panel de administración,
     * resolviendo el correo del organizador.
     */
    public List<AdminEventDTO> listAllEventsForAdmin() {
        java.util.Map<Integer, String> emailCache = new java.util.HashMap<>();
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getEventDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(event -> mapToAdminDTO(event, emailCache))
                .collect(Collectors.toList());
    }

    private void cancelEventAndCompensate(Event event) {
        Integer eventId = event.getId();

        // Obtener TODOS los inscritos con status REGISTERED o ATTENDED
        List<EventRegistration> registrations = eventRegistrationRepository
            .findByEventIdAndStatusIn(eventId, List.of("REGISTERED", "ATTENDED"));

        log.info("Registrations encontrados para evento {}: {}", eventId, registrations.size());
        registrations.forEach(r -> log.info("  - userId={} status={}", r.getUserId(), r.getStatus()));

        log.info("Eliminando evento {}. Procesando {} participantes", eventId, registrations.size());
        // Para cada inscrito: karma + notificación
        for (EventRegistration registration : registrations) {
            log.info("Procesando participante userId={}, status={}", registration.getUserId(), registration.getStatus());
            try {
                // 1. Sumar karma como si hubiera asistido
                karmaServiceClient.registerCheckIn(registration.getUserId(), eventId);
                log.info("Karma registrado exitosamente para userId={}", registration.getUserId());
            } catch (Exception e) {
                log.error("ERROR al registrar karma para userId={}: {}", registration.getUserId(), e.getMessage());
            }
            try {
                // 2. Enviar notificación
                notificationServiceClient.sendNotification(
                    registration.getUserId(),
                    "Evento cancelado",
                    "El evento '" + event.getName() + "' fue cancelado. Recibiste tus puntos de karma.",
                    "EVENT_CANCELLED"
                );
                log.info("Notificación enviada exitosamente a userId={}", registration.getUserId());
            } catch (Exception e) {
                log.error("ERROR al enviar notificación a userId={}: {}", registration.getUserId(), e.getMessage());
            }
        }

        // Cambiar status del evento a CANCELLED
        event.setStatus("CANCELADO");
        eventRepository.save(event);
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private EventResponseDTO mapToDTO(Event event, Double distance) {
        return mapToDTO(event, distance, new java.util.HashMap<>());
    }

    // Variante con cache de nombres para evitar N+1 llamadas a users-service en listados.
    private EventResponseDTO mapToDTO(Event event, Double distance, java.util.Map<Integer, String> nameCache) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setOrganizerId(event.getOrganizerId());
        dto.setName(event.getName());
        dto.setSport(event.getSport());
        dto.setEventDate(event.getEventDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setLocationName(event.getLocationName());
        dto.setMaxPlayers(event.getMaxPlayers());
        dto.setCurrentPlayers(event.getCurrentPlayers());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setFinishedAt(event.getFinishedAt());
        dto.setDistanceKm(distance);
        dto.setNombreCreador(nameCache.computeIfAbsent(event.getOrganizerId(), usersServiceClient::getNombreCreador));
        return dto;
    }

    private AdminEventDTO mapToAdminDTO(Event event, java.util.Map<Integer, String> emailCache) {
        AdminEventDTO dto = new AdminEventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setSport(event.getSport());
        dto.setEventDate(event.getEventDate());
        dto.setLocationName(event.getLocationName());
        dto.setMaxPlayers(event.getMaxPlayers());
        dto.setCurrentPlayers(event.getCurrentPlayers());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setFinishedAt(event.getFinishedAt());

        Integer organizerId = event.getOrganizerId();
        String email = emailCache.computeIfAbsent(organizerId, usersServiceClient::getEmailByUserId);
        dto.setOrganizerEmail(email);
        return dto;
    }

    private EventRegistrationDTO mapRegToDTO(EventRegistration reg) {
        EventRegistrationDTO dto = new EventRegistrationDTO();
        dto.setId(reg.getId());
        dto.setEventId(reg.getEventId());
        dto.setUserId(reg.getUserId());
        dto.setStatus(reg.getStatus());
        dto.setRegisteredAt(reg.getRegisteredAt());
        dto.setCheckedInAt(reg.getCheckedInAt());
        return dto;
    }
}
