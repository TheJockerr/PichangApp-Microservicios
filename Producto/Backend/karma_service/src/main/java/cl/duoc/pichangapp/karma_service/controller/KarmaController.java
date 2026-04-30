package cl.duoc.pichangapp.karma_service.controller;

import cl.duoc.pichangapp.karma_service.dto.CheckInEventDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaResponseDTO;
import cl.duoc.pichangapp.karma_service.dto.KarmaUpdateRequestDTO;
import cl.duoc.pichangapp.karma_service.service.KarmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/karma")
@RequiredArgsConstructor
public class KarmaController {

    private final KarmaService karmaService;

    @GetMapping("/{userId}")
    public ResponseEntity<KarmaResponseDTO> getKarma(@PathVariable String userId) {
        return ResponseEntity.ok(karmaService.getKarmaByUserId(userId));
    }

    @PostMapping("/check-in")
    public ResponseEntity<KarmaResponseDTO> registerCheckIn(@RequestBody CheckInEventDTO dto) {
        return ResponseEntity.ok(karmaService.processCheckIn(dto));
    }

    @PostMapping("/absence/{userId}/event/{eventId}")
    public ResponseEntity<KarmaResponseDTO> registerAbsence(@PathVariable String userId, @PathVariable String eventId) {
        return ResponseEntity.ok(karmaService.processAbsence(userId, eventId));
    }

    @PostMapping("/validation")
    public ResponseEntity<KarmaResponseDTO> registerOrganizerValidation(@RequestBody KarmaUpdateRequestDTO dto) {
        return ResponseEntity.ok(karmaService.processOrganizerValidation(dto));
    }
}
