package repositorios;

import modelos.PasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordTokenRepositorio extends JpaRepository<PasswordToken, Integer> {
	Optional<PasswordToken> findByToken(String token);
}
