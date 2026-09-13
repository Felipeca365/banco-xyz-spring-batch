package cl.duoc.backendiii.bffmovil.controller;

import cl.duoc.backendiii.bffmovil.dto.CuentaMovilDTO;
import cl.duoc.backendiii.common.repository.CuentaInteresRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movil/cuentas")
public class CuentaMovilController {

    private final CuentaInteresRepository cuentaInteresRepository;

    public CuentaMovilController(CuentaInteresRepository cuentaInteresRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
    }

    @GetMapping
    public List<CuentaMovilDTO> listarTodas() {
        return cuentaInteresRepository.findAll().stream()
                .map(c -> new CuentaMovilDTO(c.getCuentaId(), c.getNombre(), c.getSaldoFinal()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{cuentaId}")
    public CuentaMovilDTO buscarPorId(@PathVariable Long cuentaId) {
        return cuentaInteresRepository.findById(cuentaId)
                .map(c -> new CuentaMovilDTO(c.getCuentaId(), c.getNombre(), c.getSaldoFinal()))
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + cuentaId));
    }
}
