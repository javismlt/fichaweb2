package com.gts.fichaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.vaadin.flow.server.PWA;

@SpringBootApplication(scanBasePackages = "servicios")
@EntityScan(basePackages = "modelos")
@EnableJpaRepositories(basePackages = "repositorios") 
@Theme("my-theme")
@PWA(name = "FichaWeb", shortName = "FichaWeb", iconPath = "img/icono-144x144.png", backgroundColor = "#ffffff", themeColor = "#ffffff")
public class FichaWebApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(FichaWebApplication.class, args);
    }
}