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
    //void deleteById(Integer registroId);
    Registro findTopByUsuarioAndAccionOrderByIdDesc(Usuario usuario, String accion);
    //Registro findTopByUsuarioAndAccionAndHoraFinIsNullOrderByIdDesc(Usuario usuario, String accion);
    Registro findTopByUsuarioAndAccionInOrderByFechaRegistroDesc(Usuario usuario, List<String> acciones);
    Registro findTopByUsuarioOrderByIdDesc(Usuario usuario);
    List<Registro> findByFechaRegistroBetweenAndUsuario_IdAndActivo(LocalDate inicio, LocalDate fin, int usuarioId, int activo);
    List<Registro> findByUsuario_IdAndActivo(int usuarioId, int activo);
    List<Registro> findByFechaRegistroBetweenAndUsuario_IdAndActivoAndValidado(LocalDate fechaInicio, LocalDate fechaFin, int usuarioId, int activo, int validado);
    List<Registro> findByUsuario_IdAndActivoAndValidado(int usuarioId, int activo, int validado);
    List<Registro> findByFechaRegistroAndUsuario_IdAndActivo(LocalDate fecha, int usuarioId, int activo);
    List<Registro> findByFechaRegistroAndUsuario_IdAndActivoAndValidado(LocalDate fecha, int usuarioId, int activo, int validado);
    List<Registro> findByFechaRegistroBetweenAndActivo(LocalDate inicio, LocalDate fin, int activo);
    List<Registro> findByFechaRegistroAndActivo(LocalDate fecha, int activo);
    List<Registro> findByFechaRegistroBetweenAndActivo(LocalDate fechaInicio, LocalDate fechaFin, Integer activo);
    List<Registro> findByFechaRegistroBetweenAndActivoAndValidado(LocalDate fechaInicio, LocalDate fechaFin, Integer activo, int validado);
    List<Registro> findByFechaRegistroAndAndActivoAndValidado(LocalDate fecha, Integer activo, int validado);
    List<Registro> findByFechaRegistroBetweenAndActivoAndUsuario_Empresa_Id(LocalDate fechaInicio, LocalDate fechaFin, Integer activo, int empresaId);
    List<Registro> findByFechaRegistroBetweenAndActivoAndValidadoAndUsuario_Empresa_Id(LocalDate fechaInicio, LocalDate fechaFin, Integer activo, int validado, int empresaId);
    List<Registro> findByFechaRegistroAndActivoAndUsuario_Empresa_Id(LocalDate fecha, int activo, int empresaId);
    List<Registro> findByFechaRegistroAndAndActivoAndValidadoAndUsuario_Empresa_Id(LocalDate fecha, Integer activo, int validado, int empresaId);
    Registro findTopByUsuarioOrderByFechaRegistroDescHoraDesc(Usuario usuario);
}