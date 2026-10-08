package cl.duoc.backendiii.mstransacciones.controller;

import cl.duoc.backendiii.common.model.Transaccion;
import cl.duoc.backendiii.common.repository.TransaccionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionRepository transaccionRepository;

    // Estos valores NO estan en este proyecto: llegan desde el Config Server
    @Value("${mensaje.origen}")
    private String mensajeOrigen;

    @Value("${banco.nombre}")
    private String nombreBanco;

    @Value("${server.port}")
    private String puerto;

    public TransaccionController(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    // Endpoint de diagnostico: demuestra que la config vino del Config Server
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "servicio", "ms-transacciones",
                "banco", nombreBanco,
                "puerto", puerto,
                "configuracion", mensajeOrigen,
                "totalRegistros", transaccionRepository.count()
        );
    }

    @GetMapping
    public List<Transaccion> listarTodas() {
        return transaccionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaccion> buscarPorId(@PathVariable Long id) {
        return transaccionRepository.findById(id)
                .map(ResponseEntity::ok)                    // existe -> 200 con el dato
                .orElse(ResponseEntity.notFound().build()); // no existe -> 404
    }
}