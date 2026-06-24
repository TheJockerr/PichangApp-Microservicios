package cl.duoc.pichangapp.events_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventResponseDTO {
    private Integer id;
    private Integer organizerId;
    private String name;
    private String sport;
    private LocalDateTime eventDate;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private Double distanceKm; // Added for distance calculations
    private String nombreCreador; // Nombre + apellido del organizador (no se expone el correo)
}
