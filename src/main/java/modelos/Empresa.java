package modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "EMPRESAS")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_comercial", nullable = false, length = 255)
    private String nombreComercial;
    
    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;
    
    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;
    
    @Column(name = "cod_postal", nullable = false, length = 6)
    private String codPostal;
    
    @Column(name = "provincia", nullable = false, length = 50)
    private String provincia;
    
    @Column(name = "pais", nullable = false, length = 50)
    private String pais;
    
    @Column(name = "poblacion", nullable = false, length = 255)
    private String poblacion;
    
    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;
    
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    @Column(name = "cod_gtserp", nullable = false)
    private Integer codGtserp;
    
    @Column(name = "grupo_gtserp", nullable = false)
    private Integer grupoGtserp;
    
    @Column(name = "empresa_gtserp", nullable = false)
    private Integer empresaGtserp;
    
    @Column(name = "multiusuario", nullable = false)
    private Integer multiusuario = 0;
    
    @Column(name = "inspector", nullable = false)
    private Integer inspector = 0;
    
    @Column(name = "activo", nullable = false)
    private Integer activo = 1;
    
    @Column(name = "max_empleados", nullable = false)
    private Integer maxEmpleados;

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodPostal() {
        return codPostal;
    }

    public void setCodPostal(String codPostal) {
        this.codPostal = codPostal;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
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

    public Integer getCodGtserp() {
        return codGtserp;
    }

    public void setCodGtserp(Integer codGtserp) {
        this.codGtserp = codGtserp;
    }
    
    public Integer getGrupoGtserp() {
        return empresaGtserp;
    }

    public void setGrupoGtserp(Integer grupoGtserp) {
        this.grupoGtserp = grupoGtserp;
    }

    public Integer getEmpresaGtserp() {
        return empresaGtserp;
    }

    public void setEmpresaGtserp(Integer empresaGtserp) {
        this.empresaGtserp = empresaGtserp;
    }

    public Integer getMultiusuario() {
        return multiusuario;
    }

    public void setMultiusuario(Integer multiusuario) {
        this.multiusuario = multiusuario;
    }
    
    public Integer getInspector() {
        return inspector;
    }

    public void setInspector(Integer inspector) {
        this.inspector = inspector;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer estado) {
        this.activo = estado;
    }
    
    public Integer getMaxEmpleados() {
        return maxEmpleados;
    }

    public void setMaxEmpleados(Integer maxEmpleados) {
        this.maxEmpleados = maxEmpleados;
    }
}