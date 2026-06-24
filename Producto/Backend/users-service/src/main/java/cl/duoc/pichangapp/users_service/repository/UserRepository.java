package cl.duoc.pichangapp.users_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.pichangapp.users_service.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{
    /**
     * Busca un usuario por su correo (email).
     * Útil para login y para verificar existencia.
     */
    Optional<User> findByCorreo(String correo);

    /**
     * Indica si ya existe un usuario con el correo dado.
     * Útil en el registro para evitar duplicados.
     */
    boolean existsByCorreo(String correo);

    /**
     * Búsqueda por nombre o apellido (LIKE %texto%, sin distinguir mayúsculas).
     * Usado por el endpoint de búsqueda de usuarios.
     */
    List<User> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}

