package modelos;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "USUARIOS")
public class Usuario {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
	
	@Column(name = "telefono", length = 15)
    private String telefono;
	
	@Column(name = "email", length = 255)
    private String email;
	
	@Column(name = "nif", length = 30)
    private String nif;
	
	@Column(name = "login_usuario", nullable = false, length = 15)
    private String loginUsuario;
	
	@Column(name = "password", nullable = false, length = 255)
    private String password;
	
	@ManyToOne
	@JoinColumn(name = "empresa_id", referencedColumnName = "id", nullable = false)
	private Empresa empresa;  
	
	@Column(name = "rol", nullable = false)
    private Integer rol;
	
	@Column(name = "cod_personal")
    private Integer codPersonal;
	
	@Column(name = "pin", length = 4)
    private String pin;
	
	@Column(name = "activo", nullable = false)
    private Integer activo = 1;
	
	@Column(name = "fichaje_manual", nullable = false)
    private Integer fichajeManual = 0;
	
	@Column(name = "creado", nullable = false)
    private LocalDateTime  creado;
	
	@Column(name = "actualizado")
    private LocalDateTime  actualizado;
	
	
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getLoginUsuario() {
        return loginUsuario;
    }

    public void setLoginUsuario(String loginUsuario) {
        this.loginUsuario = loginUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Integer getRol() {
        return rol;
    }

    public void setRol(Integer rol) {
        this.rol = rol;
    }

    public Integer getCodPersonal() {
        return codPersonal;
    }

    public void setCodPersonal(Integer codPersonal) {
        this.codPersonal = codPersonal;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }

    public Integer getFichajeManual() {
        return fichajeManual;
    }

    public void setFichajeManual(Integer fichajeManual) {
        this.fichajeManual = fichajeManual;
    }
    
    public LocalDateTime getCreado() {
        return creado;
    }

    public void setCreado(LocalDateTime creado) {
        this.creado = creado;
    }

    public LocalDateTime getActualizado() {
        return actualizado;
    }

    public void setActualizado(LocalDateTime actualizado) {
        this.actualizado = actualizado;
    }
}