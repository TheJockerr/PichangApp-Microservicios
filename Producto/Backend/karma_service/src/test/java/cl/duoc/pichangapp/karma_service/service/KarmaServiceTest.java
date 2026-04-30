package cl.duoc.pichangapp.karma_service.service;

import cl.duoc.pichangapp.karma_service.dto.CheckInEventDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaResponseDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaUpdateRequestDTO;
import cl.duoc.pichangapp.karma_service.model.KarmaScore;
import cl.duoc.pichangapp.karma_service.repository.KarmaHistoryRepository;
import cl.duoc.pichangapp.karma_service.repository.KarmaScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KarmaServiceTest {

    @Mock
    private KarmaScoreRepository karmaScoreRepository;

    @Mock
    private KarmaHistoryRepository karmaHistoryRepository;

    @InjectMocks
    private KarmaService karmaService;

    private KarmaScore mockScore;
    private final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        mockScore = new KarmaScore(1, USER_ID, 100, null);
    }

    @Test
    void testGetKarmaByUserId_Existing() {
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));

        KarmaResponseDTO response = karmaService.getKarmaByUserId(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(100, response.karmaScore());
        assertEquals("Excelente", response.category());
        verify(karmaScoreRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void testProcessCheckIn_Success() {
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));
        when(karmaScoreRepository.save(any(KarmaScore.class))).thenReturn(mockScore);
        
        CheckInEventDTO dto = new CheckInEventDTO(USER_ID, "event1", "loc");
        KarmaResponseDTO response = karmaService.processCheckIn(dto);

        assertEquals(110, response.karmaScore());
        verify(karmaScoreRepository, times(1)).save(any(KarmaScore.class));
        verify(karmaHistoryRepository, times(1)).save(any());
    }

    @Test
    void testProcessAbsence_Success() {
        mockScore.setKarmaScore(20);
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));
        when(karmaScoreRepository.save(any(KarmaScore.class))).thenReturn(mockScore);

        KarmaResponseDTO response = karmaService.processAbsence(USER_ID, "event1");

        // 20 - 15 = 5
        assertEquals(5, response.karmaScore());
        assertEquals("Bajo", response.category());
    }

    @Test
    void testProcessAbsence_MinimumKarmaIsZero() {
        mockScore.setKarmaScore(10);
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));
        when(karmaScoreRepository.save(any(KarmaScore.class))).thenReturn(mockScore);

        KarmaResponseDTO response = karmaService.processAbsence(USER_ID, "event1");

        // 10 - 15 = -5, but minimum is 0
        assertEquals(0, response.karmaScore());
    }

    @Test
    void testProcessOrganizerValidation_Positive() {
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));
        when(karmaScoreRepository.save(any(KarmaScore.class))).thenReturn(mockScore);

        KarmaUpdateRequestDTO dto = new KarmaUpdateRequestDTO(USER_ID, "event1", "org1", true);
        KarmaResponseDTO response = karmaService.processOrganizerValidation(dto);

        assertEquals(105, response.karmaScore());
    }

    @Test
    void testProcessOrganizerValidation_Negative() {
        when(karmaScoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockScore));
        when(karmaScoreRepository.save(any(KarmaScore.class))).thenReturn(mockScore);

        KarmaUpdateRequestDTO dto = new KarmaUpdateRequestDTO(USER_ID, "event1", "org1", false);
        KarmaResponseDTO response = karmaService.processOrganizerValidation(dto);

        assertEquals(95, response.karmaScore());
    }
}
