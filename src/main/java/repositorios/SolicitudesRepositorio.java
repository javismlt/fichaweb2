package repositorios;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import modelos.Solicitudes;

public interface SolicitudesRepositorio extends JpaRepository<Solicitudes, Integer> {
	Optional<Solicitudes> findById(Integer id);
}
