package repositorios;

import modelos.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa, Integer> {
	 Optional<Empresa> findByNombreComercial(String nombreComercial); 
	 Optional<Empresa> findTopByOrderByIdDesc(); 
}