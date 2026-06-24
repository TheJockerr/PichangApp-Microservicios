package cl.duoc.pichangapp.events_service.controller;

import cl.duoc.pichangapp.events_service.dto.AttendanceRequest;
import cl.duoc.pichangapp.events_service.dto.CreateEventRequest;
import cl.duoc.pichangapp.events_service.dto.EventRegistrationDTO;
import cl.duoc.pichangapp.events_service.dto.EventResponseDTO;
import cl.duoc.pichangapp.events_service.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    private Integer getLoggedUserId() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return Integer.parseInt(userIdStr);
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody CreateEventRequest request) {
        Integer userId = getLoggedUserId();
        return ResponseEntity.ok(eventService.createEvent(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> findNearbyEvents(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(eventService.findNearbyEvents(lat, lng));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getEventDetails(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinEvent(@PathVariable Integer id) {
        Integer userId = getLoggedUserId();
        eventService.joinEvent(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leaveEvent(@PathVariable Integer id) {
        Integer userId = getLoggedUserId();
        try {
            eventService.leaveEvent(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        Integer userId = getLoggedUserId();
        eventService.deleteEvent(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<EventRegistrationDTO>> getEventRegistrations(@PathVariable Integer id) {
        Integer userId = getLoggedUserId();
        return ResponseEntity.ok(eventService.getEventRegistrations(id, userId));
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<String> markAttendance(
            @PathVariable Integer id,
            @RequestBody AttendanceRequest request) {
        Integer userId = getLoggedUserId();
        String message = eventService.markAttendance(id, userId, request.getUserId(), request.isAttended());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finishEvent(@PathVariable Integer id) {
        Integer userId = getLoggedUserId();
        eventService.finishEvent(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<EventResponseDTO>> getMyEvents() {
        Integer userId = getLoggedUserId();
        return ResponseEntity.ok(eventService.getMyEvents(userId));
    }

    @GetMapping("/organizing")
    public ResponseEntity<List<EventResponseDTO>> getOrganizingEvents() {
        Integer userId = getLoggedUserId();
        return ResponseEntity.ok(eventService.getOrganizingEvents(userId));
    }

    /**
     * Borrado de cuenta (uso interno desde users-service): cancela todos los eventos
     * ACTIVO del usuario y elimina sus inscripciones.
     * DELETE /api/v1/events/usuario/{userId}
     */
    @DeleteMapping("/usuario/{userId}")
    public ResponseEntity<Void> deleteEventsByUser(@PathVariable Integer userId) {
        eventService.deleteEventsByUser(userId);
        return ResponseEntity.ok().build();
    }
}
