package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import modelos.Notificaciones;
import java.util.List;

public interface NotificacionesRepositorio extends JpaRepository<Notificaciones, Integer>{
	Integer countByEstado(int estado);
	Integer countByEstadoAndSolicitanteIdAndResolucionNotNull(int estado, int usuarioId);
	List<Notificaciones> findByEstado(int estado);
	List<Notificaciones> findByEstadoAndSolicitanteIdAndResolucionNotNull(int estado, int usuarioId);
}