package cl.duoc.backendiii.bffweb.controller;

import cl.duoc.backendiii.common.model.CuentaInteres;
import cl.duoc.backendiii.common.repository.CuentaInteresRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/web/cuentas")
public class CuentaInteresController {

    private final CuentaInteresRepository cuentaInteresRepository;

    public CuentaInteresController(CuentaInteresRepository cuentaInteresRepository) {
        this.cuentaInteresRepository = cuentaInteresRepository;
    }

    @GetMapping
    public List<CuentaInteres> listarTodas() {
        return cuentaInteresRepository.findAll();
    }

    @GetMapping("/{cuentaId}")
    public CuentaInteres buscarPorId(@PathVariable Long cuentaId) {
        return cuentaInteresRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + cuentaId));
    }
}