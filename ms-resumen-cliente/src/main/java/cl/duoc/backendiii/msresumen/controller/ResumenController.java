package cl.duoc.backendiii.msresumen.controller;

import cl.duoc.backendiii.msresumen.service.TransaccionesClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resumen")
public class ResumenController {

    private final TransaccionesClient transaccionesClient;

    public ResumenController(TransaccionesClient transaccionesClient) {
        this.transaccionesClient = transaccionesClient;
    }

    @GetMapping
    public Map<String, Object> resumen() {
        return transaccionesClient.obtenerResumen();
    }
}