package servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.LocalDate;
import servicios.EmailConfigServicio;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Service
public class EmailNotificacion {
	private final EmailConfigServicio emailConfigServicio;

    public EmailNotificacion(EmailConfigServicio emailConfigService) {
        this.emailConfigServicio = emailConfigService;
    }

    public void enviarCorreoSolicitud(Integer empresaId, String solicitado, String solicitante, String tipo, String accion, String valorNuevo, LocalTime valorPrevio , LocalDate fecha) {
    	JavaMailSenderImpl mailSender = emailConfigServicio.getMailSenderByEmpresaId(empresaId);
    	
    	String body = "El usuario " + solicitante + ", ha solicitado la " + tipo + " del registro con fecha " + fecha + " y acción " + accion + ", cambio de hora " + valorPrevio + " a " + valorNuevo + ".";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(solicitado);
        message.setSubject("Solicitud de " + tipo + ", " + solicitante);
        message.setText(body);
        message.setFrom(mailSender.getUsername());
        mailSender.send(message);
    }
    
    public void enviarCorreoPermiso(Integer empresaId, String solicitado, String solicitante, String tipo, LocalDate fecha, LocalDate fechaAux) {
    	JavaMailSenderImpl mailSender = emailConfigServicio.getMailSenderByEmpresaId(empresaId);
    	
    	String body;
        if(fechaAux == null) {
        	body = "El usuario " + solicitante + " ha solicitado el permiso de " + tipo + " para la fecha " + fecha + ".";
        } else {
        	body = "El usuario " + solicitante + " ha solicitado el permiso de " + tipo + " para las fechas " + fecha + " - " + fechaAux + ".";
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(solicitado);
        message.setSubject("Permiso de " + tipo + ", " + solicitante);
        message.setText(body);
        message.setFrom(mailSender.getUsername()); 
        mailSender.send(message);
    }
    
    public void enviarCorreoRespuestaSolicitud(Integer empresaId, String nombre, String solicitante, LocalDate fecha, String accion, String previoValor, String nuevoValor, String Estado) {
    	JavaMailSenderImpl mailSender = emailConfigServicio.getMailSenderByEmpresaId(empresaId);
    	
    	String body = "Su solicitud de modificación de registro con fecha " + fecha + " y acción " + accion + " ha sido " + Estado + ".";
        if(Estado == "ACEPTADO") {
        	body += " Modificación de hora " + previoValor + " a " + nuevoValor + ".";
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(solicitante);
        message.setSubject("Resolución solicitud de modificación " + fecha + ", " + nombre);
        message.setText(body);
        message.setFrom(mailSender.getUsername()); 
        mailSender.send(message);
    }
    
    public void enviarCorreoRespuestaPermiso(Integer empresaId, String nombre, String solicitante, String motivo, LocalDate fecha, LocalDate fechaAux, String Estado) {
    	JavaMailSenderImpl mailSender = emailConfigServicio.getMailSenderByEmpresaId(empresaId);
    	
    	String body;
        if(fechaAux == null) {
        	body = "Su solicitud de permiso con motivo " + motivo + " en la fecha " + fecha + " ha sido " + Estado + ".";
        } else {
        	body = "Su solicitud de permiso con motivo " + motivo + " en las fechas " + fecha + " - " + fechaAux + " ha sido " + Estado + ".";
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(solicitante);
        message.setSubject("Resolución solicitud de permiso " + motivo + ", " + nombre);
        message.setText(body);
        message.setFrom(mailSender.getUsername()); 
        mailSender.send(message);
    }
}