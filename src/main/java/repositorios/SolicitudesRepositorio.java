package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import modelos.Solicitudes;

public interface SolicitudesRepositorio extends JpaRepository<Solicitudes, Integer> {

}
