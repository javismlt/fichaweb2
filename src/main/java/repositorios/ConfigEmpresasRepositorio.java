package repositorios;

import modelos.ConfigEmpresas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfigEmpresasRepositorio extends JpaRepository<ConfigEmpresas, Integer> {
	Optional<ConfigEmpresas> findByEmpresaId_Id(Integer empresaId);
}