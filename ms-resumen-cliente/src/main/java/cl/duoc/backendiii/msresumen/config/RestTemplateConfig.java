package cl.duoc.backendiii.msresumen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced   // Permite usar "http://ms-transacciones/..." -> Eureka resuelve la direccion real
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${transacciones.usuario}") String usuario,
                                     @Value("${transacciones.clave}") String clave) {
        return builder
                .basicAuthentication(usuario, clave)        // envia Basic Auth en cada llamada
                .setConnectTimeout(Duration.ofSeconds(2))   // no esperar eternamente para conectar
                .setReadTimeout(Duration.ofSeconds(3))      // ni para recibir respuesta
                .build();
    }
}