package modelos;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "NOTIFICACIONES")
public class Notificaciones {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "asociado_id", nullable = false)
	private Integer idAsociado;
	
	@ManyToOne
    @JoinColumn(name = "solicitante_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitante;
	
	@ManyToOne
    @JoinColumn(name = "solicitado_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitado;
	
	@Column(name = "tipo", nullable = false)
	private String tipo;
	
	@Column(name = "estado", nullable = false)
	private Integer estado;
	
	@Column(name = "resolucion")
	private String resolucion;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getidAsociado() {
        return idAsociado;
    }

    public void setidAsociado(Integer idAsociado) {
        this.idAsociado = idAsociado;
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

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
    
    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }
}