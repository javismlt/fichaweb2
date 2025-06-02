package repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import modelos.CustomFields;
import java.util.List;
import modelos.Empresa;
import java.util.Optional;

public interface CustomFieldsRepositorio extends JpaRepository<CustomFields, Integer> {
	List<CustomFields> findByEmpresa_IdAndUsuario_Id(Integer empresaId, Integer usuarioId);
	List<CustomFields> findByEmpresa_Id(Integer empresaId);
	Optional<CustomFields> findByEmpresaAndName(Empresa empresa, String name);
}