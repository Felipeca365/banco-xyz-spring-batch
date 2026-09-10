package cl.duoc.backendiii.bffcajero.controller;

import cl.duoc.backendiii.bffcajero.dto.EstadoAnualCajeroDTO;
import cl.duoc.backendiii.common.repository.EstadoCuentaAnualRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cajero/estado-anual")
public class EstadoAnualCajeroController {

    private final EstadoCuentaAnualRepository estadoCuentaAnualRepository;

    public EstadoAnualCajeroController(EstadoCuentaAnualRepository estadoCuentaAnualRepository) {
        this.estadoCuentaAnualRepository = estadoCuentaAnualRepository;
    }

    @GetMapping("/{cuentaId}")
    public EstadoAnualCajeroDTO consultarEstadoAnual(@PathVariable Long cuentaId) {
        return estadoCuentaAnualRepository.findById(cuentaId)
                .map(e -> new EstadoAnualCajeroDTO(e.getCuentaId(), e.getSaldoNetoAnual(), e.getCantidadTransacciones()))
                .orElseThrow(() -> new RuntimeException("Estado anual no encontrado para cuenta id: " + cuentaId));
    }
}