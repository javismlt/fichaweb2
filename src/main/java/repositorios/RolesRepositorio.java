package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import modelos.Roles;

public interface RolesRepositorio extends JpaRepository<Roles, Integer> {
}