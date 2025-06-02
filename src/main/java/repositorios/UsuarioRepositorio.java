package repositorios;

import modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import modelos.Roles;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
	Usuario findByLoginUsuario(String loginUsuario);
	Optional<Usuario> findByEmail(String email);
	Usuario findByEmpresaIdAndRolId(Integer empresaId, int rolId);
	List<Usuario> findByEmpresaId(Integer empresaId);
	void deleteByEmpresaId(Integer empresaId);
	boolean existsByCodPersonalAndEmpresa_Id(int codPersonal, int empresaId);
	int countByEmpresaIdAndActivo(Integer id_empresa, int activo);
	Optional<Usuario> findByEmpresa_IdAndRol_Id(Integer empresaId, int rolId);
	Usuario findByRolAndEmpresa_Id(Roles rol, Integer empresaId);
}