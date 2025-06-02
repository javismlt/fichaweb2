package servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import servicios.EmailConfigServicio;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Service
public class EmailServicio {
	private final EmailConfigServicio emailConfigServicio;
	
	public EmailServicio(EmailConfigServicio emailConfigService) {
        this.emailConfigServicio = emailConfigService;
    }

    public void enviarCorreo(Integer empresaId, String to, String token) {
    	JavaMailSenderImpl mailSender = emailConfigServicio.getMailSenderByEmpresaId(empresaId);
    	
        String url = "http://localhost:8080/cambiarpasswd?token=" + token;
        String body = "FichaWeb\n\nHaz clic en el siguiente enlace para restablecer tu contraseña:\n" + url + "\n\nEste enlace expirará en 30 minutos.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperación de contraseña");
        message.setText(body);
        message.setFrom(mailSender.getUsername()); 
        mailSender.send(message);
    }
}