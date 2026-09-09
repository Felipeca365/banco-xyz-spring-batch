package cl.duoc.backendiii.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "estados_cuenta_anuales")
public class EstadoCuentaAnual {

    @Id
    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "total_depositos")
    private BigDecimal totalDepositos;

    @Column(name = "total_retiros_compras_pagos")
    private BigDecimal totalRetirosComprasPagos;

    @Column(name = "saldo_neto_anual")
    private BigDecimal saldoNetoAnual;

    @Column(name = "cantidad_transacciones")
    private Integer cantidadTransacciones;

    @Column(name = "cantidad_anomalias")
    private Integer cantidadAnomalias;

    // Getters y setters
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public BigDecimal getTotalDepositos() { return totalDepositos; }
    public void setTotalDepositos(BigDecimal totalDepositos) { this.totalDepositos = totalDepositos; }

    public BigDecimal getTotalRetirosComprasPagos() { return totalRetirosComprasPagos; }
    public void setTotalRetirosComprasPagos(BigDecimal totalRetirosComprasPagos) { this.totalRetirosComprasPagos = totalRetirosComprasPagos; }

    public BigDecimal getSaldoNetoAnual() { return saldoNetoAnual; }
    public void setSaldoNetoAnual(BigDecimal saldoNetoAnual) { this.saldoNetoAnual = saldoNetoAnual; }

    public Integer getCantidadTransacciones() { return cantidadTransacciones; }
    public void setCantidadTransacciones(Integer cantidadTransacciones) { this.cantidadTransacciones = cantidadTransacciones; }

    public Integer getCantidadAnomalias() { return cantidadAnomalias; }
    public void setCantidadAnomalias(Integer cantidadAnomalias) { this.cantidadAnomalias = cantidadAnomalias; }
}