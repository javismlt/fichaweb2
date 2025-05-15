package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import modelos.Logs_modificaciones;

@Repository
public interface Logs_modificacionesRepositorio extends JpaRepository<Logs_modificaciones, Integer>{

}
