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

    @Column(nullable = false, length = 20)
    private String role = "USER"; // Valores: "USER" o "ADMIN"

    @Column(length = 6)
    private String verificationCode;

    @Column
    private java.time.LocalDateTime verificationCodeExpiry;

    // Controla si el historial de karma del usuario es visible en su perfil público.
    // columnDefinition con default para no romper filas existentes al agregar la columna.
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private Boolean historialVisible = true;
}
