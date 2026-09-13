package cl.duoc.backendiii.bffmovil.dto;

import java.math.BigDecimal;

public class EstadoAnualMovilDTO {

    private Long cuentaId;
    private BigDecimal saldoNetoAnual;

    public EstadoAnualMovilDTO(Long cuentaId, BigDecimal saldoNetoAnual) {
        this.cuentaId = cuentaId;
        this.saldoNetoAnual = saldoNetoAnual;
    }

    public Long getCuentaId() { return cuentaId; }
    public BigDecimal getSaldoNetoAnual() { return saldoNetoAnual; }
}
