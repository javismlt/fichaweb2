package modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "CUSTOM_FIELDS")
public class CustomFields {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "empresa_id", referencedColumnName = "id", nullable = false)
    private Empresa empresa;
	
	@ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;
	
	@Column(name = "label", nullable = false)
	private String label;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "valor")
	private String valor;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Empresa getEmpresaId() {
        return empresa;
    }

    public void setEmpresaId(Empresa empresa) {
        this.empresa = empresa;
    }
    
    public Usuario getUsuarioId() {
        return usuario;
    }

    public void setUsuarioId(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}