package modelos;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "LOGS_MODIFICACIONES")
public class Logs_modificaciones {
		
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;
		
		@Column(name = "fecha", nullable = false)
	    private LocalDate fecha;
		
		@ManyToOne
	    @JoinColumn(name = "registro_id", referencedColumnName = "id", nullable = false)
	    private Registro registro;
		 
		@Column(name = "campo_modificado", nullable = false)
		private String campoModificado;
		 
		@Column(name = "valor_previo", nullable = false)
		private String valorPrevio;
		 
		@Column(name = "valor_nuevo", nullable = false)
		private String valorNuevo;
		
		
		public Integer getId() {
	        return id;
	    }

	    public void setId(Integer id) {
	        this.id = id;
	    }

	    public LocalDate getFecha() {
	        return fecha;
	    }

	    public void setFecha(LocalDate fecha) {
	        this.fecha = fecha;
	    }

	    public Registro getRegistro() {
	        return registro;
	    }

	    public void setRegistro(Registro registro) {
	        this.registro = registro;
	    }

	    public String getCampo() {
	        return campoModificado;
	    }

	    public void setCampo(String campoModificado) {
	        this.campoModificado = campoModificado;
	    }

	    public String getValorPrevio() {
	        return valorPrevio;
	    }

	    public void setValorPrevio(String valorPrevio) {
	        this.valorPrevio = valorPrevio;
	    }

	    public String getValorNuevo() {
	        return valorNuevo;
	    }

	    public void setValorNuevo(String valorNuevo) {
	        this.valorNuevo = valorNuevo;
	    }
}