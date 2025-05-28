package modelos;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "PERMISOS")
public class Permisos {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "solicitante_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitante;
	
	@ManyToOne
    @JoinColumn(name = "solicitado_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitado;
	
	@Column(name = "tipo", nullable = false)
	private String tipo;
	
	@Column(name = "estado", nullable = false)
	private String estado;
	
	@Column(name = "fecha", nullable = false)
    private LocalDate fecha;
	
	@Column(name = "fechaAux")
    private LocalDate fechaAux;
	
	@Column(name = "motivo", nullable = false)
	private String motivo;
	
	@Column(name = "observaciones", nullable = false)
	private String observaciones;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getSolicitado() {
        return solicitado;
    }

    public void setSolicitado(Usuario solicitado) {
        this.solicitado = solicitado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaAux() {
        return fechaAux;
    }

    public void setFechaAux(LocalDate fechaAux) {
        this.fechaAux = fechaAux;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}