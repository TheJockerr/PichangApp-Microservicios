package cl.duoc.pichangapp.events_service.service;

import cl.duoc.pichangapp.events_service.dto.CreateEventRequest;
import cl.duoc.pichangapp.events_service.dto.EventResponseDTO;
import cl.duoc.pichangapp.events_service.model.Event;
import cl.duoc.pichangapp.events_service.model.EventRegistration;
import cl.duoc.pichangapp.events_service.repository.EventRegistrationRepository;
import cl.duoc.pichangapp.events_service.repository.EventRepository;
import cl.duoc.pichangapp.events_service.scheduler.EventScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private KarmaServiceClient karmaServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private EventService eventService;

    @InjectMocks
    private EventScheduler eventScheduler;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1);
        event.setOrganizerId(10);
        event.setName("Pichanga Test");
        event.setStatus("ACTIVE");
        event.setMaxPlayers(10);
        event.setCurrentPlayers(0);
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLatitude(-33.4569);
        event.setLongitude(-70.6482);
    }

    @Test
    void createEvent_Success() {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Pichanga Test");
        request.setMaxPlayers(10);

        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponseDTO response = eventService.createEvent(request, 10);

        assertNotNull(response);
        assertEquals("Pichanga Test", response.getName());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void joinEvent_Full_ThrowsException() {
        event.setCurrentPlayers(10); // Full
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            eventService.joinEvent(1, 20);
        });

        assertEquals("Event is full", ex.getMessage());
    }

    @Test
    void deleteEvent_Success() {
        EventRegistration reg = new EventRegistration();
        reg.setEventId(1);
        reg.setUserId(20);
        reg.setStatus("REGISTERED");

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(eventRegistrationRepository.findByEventIdAndStatusIn(eq(1), anyList())).thenReturn(List.of(reg));

        eventService.deleteEvent(1, 10);

        assertEquals("CANCELLED", event.getStatus());
        verify(karmaServiceClient, times(1)).registerCheckIn(20, 1);
        verify(notificationServiceClient, times(1)).sendNotification(eq(20), any(), any(), any());
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void processFinishedEvents_MarksAbsence() {
        Event oldEvent = new Event();
        oldEvent.setId(1);
        oldEvent.setStatus("ACTIVE");
        oldEvent.setEventDate(LocalDateTime.now().minusHours(5));

        EventRegistration reg = new EventRegistration();
        reg.setUserId(20);
        reg.setEventId(1);
        reg.setStatus("REGISTERED");

        when(eventRepository.findByStatusAndEventDateBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenReturn(List.of(oldEvent));
        when(eventRegistrationRepository.findByEventId(1)).thenReturn(List.of(reg));

        eventScheduler = new EventScheduler(eventRepository, eventRegistrationRepository, karmaServiceClient);
        eventScheduler.processFinishedEvents();

        assertEquals("FINISHED", oldEvent.getStatus());
        assertEquals("ABSENT", reg.getStatus());
        verify(karmaServiceClient, times(1)).registerAbsence(20, 1);
        verify(eventRepository, times(1)).save(oldEvent);
        verify(eventRegistrationRepository, times(1)).save(reg);
    }
}
