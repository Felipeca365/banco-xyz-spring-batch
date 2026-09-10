package cl.duoc.backendiii.bffcajero.controller;

import cl.duoc.backendiii.bffcajero.dto.SaldoCajeroDTO;
import cl.duoc.backendiii.common.repository.CuentaInteresRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cajero/saldo")
public class SaldoCajeroController {

    private final CuentaInteresRepository cuentaInteresRepository;

    public SaldoCajeroController(CuentaInteresRepository cuentaInteresRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
    }

    @GetMapping("/{cuentaId}")
    public SaldoCajeroDTO consultarSaldo(@PathVariable Long cuentaId) {
        return cuentaInteresRepository.findById(cuentaId)
                .map(c -> new SaldoCajeroDTO(c.getCuentaId(), c.getNombre(), c.getSaldoFinal()))
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + cuentaId));
    }
}