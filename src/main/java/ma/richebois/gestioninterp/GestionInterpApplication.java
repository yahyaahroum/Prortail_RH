package ma.richebois.gestioninterp;

import ma.richebois.gestioninterp.Domaine.SecurityAuditorAware;
import ma.richebois.gestioninterp.Model.*;
import ma.richebois.gestioninterp.Repository.*;
import ma.richebois.gestioninterp.Service.AjoutServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.*;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef="auditorAware")
public class GestionInterpApplication implements CommandLineRunner {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new SecurityAuditorAware();
    }

    public static void main(String[] args) {
        SpringApplication.run(GestionInterpApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


    }
}
