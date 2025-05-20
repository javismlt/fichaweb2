package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import modelos.Logs_modificaciones;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface Logs_modificacionesRepositorio extends JpaRepository<Logs_modificaciones, Integer>{
	 List<Logs_modificaciones> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
