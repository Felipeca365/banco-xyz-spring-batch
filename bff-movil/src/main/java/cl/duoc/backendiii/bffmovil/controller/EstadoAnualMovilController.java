package cl.duoc.backendiii.bffmovil.controller;

import cl.duoc.backendiii.bffmovil.dto.EstadoAnualMovilDTO;
import cl.duoc.backendiii.common.repository.EstadoCuentaAnualRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movil/estados-anuales")
public class EstadoAnualMovilController {

    private final EstadoCuentaAnualRepository estadoCuentaAnualRepository;

    public EstadoAnualMovilController(EstadoCuentaAnualRepository estadoCuentaAnualRepository) {
        this.estadoCuentaAnualRepository = estadoCuentaAnualRepository;
    }

    @GetMapping
    public List<EstadoAnualMovilDTO> listarTodos() {
        return estadoCuentaAnualRepository.findAll().stream()
                .map(e -> new EstadoAnualMovilDTO(e.getCuentaId(), e.getSaldoNetoAnual()))
                .collect(Collectors.toList());
    }
}
