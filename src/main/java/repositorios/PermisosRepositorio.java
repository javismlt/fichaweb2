package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import modelos.Permisos;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public interface PermisosRepositorio extends JpaRepository<Permisos, Integer> {
	List<Permisos> findBySolicitanteId(Integer solicitanteId);
	Optional<Permisos> findById(Integer id);
}
