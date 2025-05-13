package modelos;

import jakarta.persistence.*;
import java.time.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "REGISTROS")
public class Registro {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;
	
	@Column(name = "id_asociado", nullable = true)
	private Integer idasociado;
	
	@Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
	
	@Column(name = "hora", nullable = true)
	@DateTimeFormat(pattern = "HH:mm:ss")
	private LocalTime hora;
	 
	@Column(name = "accion", nullable = false, length = 15)
	private String accion;
	 
	@Column(name = "observaciones", length = 255)
	private String observaciones;
	 
	@Column(name = "validado", nullable = false)
	private Integer validado = 0;
	
	@Column(name = "origen", length = 10)
	private String origen;

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Usuario getUsuarioId() {
		return usuario;
	}

	public void setUsuarioId(Usuario usuarioId) {
		this.usuario = usuarioId;
	}

	public Integer getIdAsociado() {
	    return idasociado;
	}

	public void setIdAsociado(Integer idasociado) {
	    this.idasociado = idasociado;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public LocalTime getHora() {
		return hora;
	}

	public void setHora(LocalTime hora) {
		this.hora = hora;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Integer getValidado() {
		return validado;
	}

	public void setValidado(Integer validado) {
		this.validado = validado;
	}
	
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

}