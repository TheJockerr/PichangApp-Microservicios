package cl.duoc.pichangapp.karma_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "karma_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KarmaScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", unique = true, nullable = false, length = 100)
    private String userId;

    @Builder.Default
    @Column(name = "karma_score", nullable = false)
    private Integer karmaScore = 100;

    @OneToMany(mappedBy = "karmaScore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KarmaHistory> history;
}
