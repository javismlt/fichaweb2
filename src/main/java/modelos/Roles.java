package modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "ROLES")
public class Roles {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	    
	@Column(name = "nombre", nullable = false)
	private String nombre;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
