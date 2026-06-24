package cl.duoc.pichangapp.events_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer organizerId;      // userId del creador
    private String name;              // nombre del partido
    private String sport;             // Fútbol, Básquetbol, etc.
    private LocalDateTime eventDate;  // fecha y hora del evento
    private Double latitude;          // coordenada lat
    private Double longitude;         // coordenada lng
    private String locationName;      // nombre legible del lugar
    private Integer maxPlayers;       // máximo de jugadores
    private Integer currentPlayers;   // jugadores inscritos actualmente
    private String status;            // ACTIVO, FINALIZADO, CANCELADO
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt; // cuando se marca como finalizado
}
