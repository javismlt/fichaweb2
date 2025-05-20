package repositorios;

import modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
	Usuario findByLoginUsuario(String loginUsuario);
	Usuario findByEmpresaIdAndRolId(Integer empresaId, int rolId);
	Optional<Usuario> findByEmail(String email);
	List<Usuario> findByEmpresaId(Integer empresaId);
	void deleteByEmpresaId(Integer empresaId);
	boolean existsByCodPersonalAndEmpresa_Id(int codPersonal, int empresaId);
	int countByEmpresaIdAndActivo(Integer id_empresa, int activo);
	Optional<Usuario> findByEmpresa_IdAndRol_Id(Integer empresaId, int rolId);
}