package cl.duoc.backendiii.bffweb.controller;

import cl.duoc.backendiii.common.model.EstadoCuentaAnual;
import cl.duoc.backendiii.common.repository.EstadoCuentaAnualRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/web/estados-anuales")
public class EstadoCuentaAnualController {

    private final EstadoCuentaAnualRepository estadoCuentaAnualRepository;

    public EstadoCuentaAnualController(EstadoCuentaAnualRepository estadoCuentaAnualRepository) {
        this.estadoCuentaAnualRepository = estadoCuentaAnualRepository;
    }

    @GetMapping
    public List<EstadoCuentaAnual> listarTodos() {
        return estadoCuentaAnualRepository.findAll();
    }

    @GetMapping("/{cuentaId}")
    public EstadoCuentaAnual buscarPorId(@PathVariable Long cuentaId) {
        return estadoCuentaAnualRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Estado de cuenta anual no encontrado para cuenta id: " + cuentaId));
    }
}