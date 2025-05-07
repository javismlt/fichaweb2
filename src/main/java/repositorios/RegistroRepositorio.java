package repositorios;

import modelos.Registro;
import modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface RegistroRepositorio extends JpaRepository<Registro, Integer> {
    List<Registro> findByFechaRegistroBetweenAndUsuario_Id(LocalDate fechaInicio, LocalDate fechaFin, Integer usuarioId);
    List<Registro> findByUsuario_Id(Integer usuarioId);
    void deleteByUsuario_Id(Integer usuarioId);
    Registro findTopByUsuarioAndAccionOrderByIdDesc(Usuario usuario, String accion);
    Registro findTopByUsuarioAndAccionAndHoraFinIsNullOrderByIdDesc(Usuario usuario, String accion);
    Registro findTopByUsuarioAndAccionInOrderByFechaRegistroDesc(Usuario usuario, List<String> acciones);
    Registro findTopByUsuarioOrderByIdDesc(Usuario usuario);
}