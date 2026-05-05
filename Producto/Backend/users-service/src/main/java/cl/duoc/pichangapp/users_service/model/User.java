package cl.duoc.pichangapp.users_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    // Identificador numérico autoincremental

    @Column(nullable = false, length = 100, unique = true)
    private String correo; // Correo único para login

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String contrasena; // Contraseña encriptada

    @Column(nullable = false, length = 50)
    private String nombre; // Nombre del usuario

    @Column(nullable = false, length = 50)
    private String apellido; // Apellido del usuario

    @Column(nullable = false)
    private boolean enabled = false;
    // Estado de verificación de la cuenta (true cuando el usuario está validado)
}
