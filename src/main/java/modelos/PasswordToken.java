package modelos;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "PASSWORD_TOKEN")
public class PasswordToken {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	    
	@Column(name = "token", nullable = false, length = 255)
	private String token;
	    
	@Column(name = "email", nullable = false, length = 255)
	private String email;
	
	@Column(name = "expiracion", nullable = false)
	private LocalDateTime expiracion;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getExpiration() {
        return expiracion;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiracion = expiration;
    }
}