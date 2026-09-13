package cl.duoc.backendiii.bffmovil.controller;

import cl.duoc.backendiii.bffmovil.dto.TransaccionMovilDTO;
import cl.duoc.backendiii.common.repository.TransaccionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movil/transacciones")
public class TransaccionMovilController {

    private final TransaccionRepository transaccionRepository;

    public TransaccionMovilController(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    @GetMapping
    public List<TransaccionMovilDTO> listarTodas() {
        return transaccionRepository.findAll().stream()
                .map(t -> new TransaccionMovilDTO(t.getId(), t.getFecha(), t.getMonto(), t.getTipo()))
                .collect(Collectors.toList());
    }
}
