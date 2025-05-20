package modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "SOLICITUDES")
public class Solicitudes {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	    
	@ManyToOne
    @JoinColumn(name = "registro_id", referencedColumnName = "id", nullable = false)
    private Registro registro;
	
	@ManyToOne
    @JoinColumn(name = "solicitante_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitante;
	
	@ManyToOne
    @JoinColumn(name = "solicitado_id", referencedColumnName = "id", nullable = false)
    private Usuario solicitado;
	
	@Column(name = "tipo", nullable = false)
	private String tipo;
	
	@Column(name = "campo", nullable = false)
	private String campo;
	
	@Column(name = "valor", nullable = false)
	private String valor;
	
	@Column(name = "accion", nullable = false)
	private String accion;
	
	@Column(name = "estado", nullable = false)
	private String estado;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Registro getRegistro() {
        return registro;
    }

    public void setRegistro(Registro registro) {
        this.registro = registro;
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

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}