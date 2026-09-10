package cl.duoc.backendiii.bffcajero.dto;

import java.math.BigDecimal;

public class EstadoAnualCajeroDTO {

    private Long cuentaId;
    private BigDecimal saldoNetoAnual;
    private Integer cantidadTransacciones;

    public EstadoAnualCajeroDTO(Long cuentaId, BigDecimal saldoNetoAnual, Integer cantidadTransacciones) {
        this.cuentaId = cuentaId;
        this.saldoNetoAnual = saldoNetoAnual;
        this.cantidadTransacciones = cantidadTransacciones;
    }

    public Long getCuentaId() { return cuentaId; }
    public BigDecimal getSaldoNetoAnual() { return saldoNetoAnual; }
    public Integer getCantidadTransacciones() { return cantidadTransacciones; }
}