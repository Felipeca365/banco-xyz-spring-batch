package cl.duoc.backendiii.mstransacciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "cl.duoc.backendiii.common.model")
@EnableJpaRepositories(basePackages = "cl.duoc.backendiii.common.repository")
public class MsTransaccionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsTransaccionesApplication.class, args);
    }
}