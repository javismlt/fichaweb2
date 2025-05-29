package servicios;

import modelos.ConfigEmpresas;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import repositorios.ConfigEmpresasRepositorio;
import java.util.Optional;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailConfigServicio {

    private final ConfigEmpresasRepositorio configEmpresasRepositorio;
    
    @Value("${MAIL_HOST}")
    private String defaultMailHost;
    
    @Value("${MAIL_PORT}")
    private int defaultMailPort;

    @Value("${MAIL_USERNAME}")
    private String defaultMailUser;

    @Value("${MAIL_PASSWORD}")
    private String defaultMailPassword;

    public EmailConfigServicio(ConfigEmpresasRepositorio configEmpresasRepositorio) {
        this.configEmpresasRepositorio = configEmpresasRepositorio;
    }

    public JavaMailSenderImpl getMailSenderByEmpresaId(Integer empresaId) {
        ConfigEmpresas config = getConfigByEmpresaId(empresaId);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getMailHost());
        mailSender.setPort(config.getMailPort());
        mailSender.setUsername(config.getMailUser());
        mailSender.setPassword(config.getMailPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    private ConfigEmpresas getConfigByEmpresaId(Integer empresaId) {
        Optional<ConfigEmpresas> optional = configEmpresasRepositorio.findByEmpresaId_Id(empresaId);
        return optional.orElseGet(this::getDefectoConfig);
    }

    private ConfigEmpresas getDefectoConfig() {
        ConfigEmpresas config = new ConfigEmpresas();
        config.setMailHost(defaultMailHost);
        config.setMailPort(defaultMailPort);
        config.setMailUser(defaultMailUser);
        config.setMailPassword(defaultMailPassword);
        return config;
    }
}