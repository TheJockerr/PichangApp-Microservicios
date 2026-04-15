package cl.duoc.pichangapp.users_service.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.pichangapp.users_service.Model.User;

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
}
