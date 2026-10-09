package cl.duoc.backendiii.msresumen.service;

import cl.duoc.backendiii.msresumen.dto.TransaccionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TransaccionesClient {

    private final RestTemplate restTemplate;

    // Ultimo resumen exitoso: se usa como dato de respaldo en el fallback
    private volatile Map<String, Object> ultimoResumenConocido;

    public TransaccionesClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // name = el circuit breaker configurado en el Config Server
    // fallbackMethod = metodo que responde si la llamada falla o el circuito esta abierto
    @CircuitBreaker(name = "transacciones", fallbackMethod = "resumenFallback")
    public Map<String, Object> obtenerResumen() {

        TransaccionDTO[] transacciones = restTemplate.getForObject(
                "http://ms-transacciones/api/transacciones",   // nombre en Eureka, no localhost:8091
                TransaccionDTO[].class);

        if (transacciones == null) {
            transacciones = new TransaccionDTO[0];
        }

        BigDecimal montoTotal = Arrays.stream(transacciones)
                .map(TransaccionDTO::monto)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long anomalias = Arrays.stream(transacciones)
                .filter(t -> Boolean.TRUE.equals(t.esAnomalia()))
                .count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("estado", "OK");
        resumen.put("origen", "ms-transacciones (datos en vivo via Eureka)");
        resumen.put("cantidadTransacciones", transacciones.length);
        resumen.put("montoTotal", montoTotal);
        resumen.put("cantidadAnomalias", anomalias);
        resumen.put("consultadoEn", LocalDateTime.now().toString());

        ultimoResumenConocido = resumen;   // guardamos copia de respaldo
        return resumen;
    }

    // FALLBACK: misma respuesta (Map) + un parametro Throwable con la causa del fallo
    public Map<String, Object> resumenFallback(Throwable ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estado", "DEGRADADO");
        respuesta.put("mensaje", "ms-transacciones no disponible. Respuesta de respaldo (fallback).");
        respuesta.put("causa", ex.getClass().getSimpleName());
        respuesta.put("ultimoResumenConocido", ultimoResumenConocido);   // puede ser null
        respuesta.put("respondidoEn", LocalDateTime.now().toString());
        return respuesta;
    }
}