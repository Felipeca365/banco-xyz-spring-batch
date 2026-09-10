package cl.duoc.backendiii.bffweb.controller;

import cl.duoc.backendiii.common.model.Transaccion;
import cl.duoc.backendiii.common.repository.TransaccionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/web/transacciones")
public class TransaccionController {
    
    private final TransaccionRepository transaccionRepository;

    public TransaccionController(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    @GetMapping 
    public List<Transaccion> listarTodas() {
        return transaccionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Transaccion buscarPorId(@PathVariable Long id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada con id " + id));
    }
}
