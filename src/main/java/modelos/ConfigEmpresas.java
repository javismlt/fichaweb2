package modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "CONFIG_EMPRESAS")
public class ConfigEmpresas {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "empresa_id", referencedColumnName = "id", nullable = false)
    private Empresa empresaId;
	
	@Column(name = "mail_host", nullable = false)
	private String mailHost;
	
	@Column(name = "mail_port", nullable = false)
	private Integer mailPort;
	
	@Column(name = "mail_user", nullable = false)
	private String mailUser;
	
	@Column(name = "mail_password", nullable = false)
	private String mailPassword;
	
	
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Empresa getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Empresa empresaId) {
        this.empresaId = empresaId;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }
    
    public Integer getMailPort() {
        return mailPort;
    }

    public void setMailPort(Integer mailPort) {
        this.mailPort = mailPort;
    }
    
    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }
    
    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }
}