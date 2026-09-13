package cl.duoc.backendiii.bffweb.controller;

import cl.duoc.backendiii.common.model.TransaccionAnualDetalle;
import cl.duoc.backendiii.common.repository.TransaccionAnualDetalleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/web/transacciones-anuales")
public class TransaccionAnualDetalleController {

    private final TransaccionAnualDetalleRepository transaccionAnualDetalleRepository;

    public TransaccionAnualDetalleController(TransaccionAnualDetalleRepository transaccionAnualDetalleRepository) {
        this.transaccionAnualDetalleRepository = transaccionAnualDetalleRepository;
    }

    @GetMapping
    public List<TransaccionAnualDetalle> listarTodas() {
        return transaccionAnualDetalleRepository.findAll();
    }

    @GetMapping("/cuenta/{cuentaId}")
    public List<TransaccionAnualDetalle> listarPorCuenta(@PathVariable Long cuentaId) {
        return transaccionAnualDetalleRepository.findAll().stream()
                .filter(t -> t.getCuentaId().equals(cuentaId))
                .collect(Collectors.toList());
    }
}