package cl.duoc.backendiii.msresumen.dto;

import java.math.BigDecimal;

// Molde para recibir el JSON que entrega ms-transacciones
public record TransaccionDTO(
        Long id,
        String fecha,
        BigDecimal monto,
        String tipo,
        Boolean esAnomalia,
        String motivoAnomalia
) {}