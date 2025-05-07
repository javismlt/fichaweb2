package servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreo(String to, String token) {
        String url = "http://localhost:8080/cambiarpasswd?token=" + token;
        String body = "FichaWeb\n\nHaz clic en el siguiente enlace para restablecer tu contraseña:\n" + url + "\n\nEste enlace expirará en 30 minutos.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperación de contraseña");
        message.setText(body);

        mailSender.send(message);
    }
}