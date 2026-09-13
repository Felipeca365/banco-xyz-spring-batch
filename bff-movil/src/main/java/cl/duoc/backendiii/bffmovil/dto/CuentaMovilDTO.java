package cl.duoc.backendiii.bffmovil.dto;

import java.math.BigDecimal;

public class CuentaMovilDTO {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldoFinal;

    public CuentaMovilDTO(Long cuentaId, String nombre, BigDecimal saldoFinal) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldoFinal = saldoFinal;
    }

    public Long getCuentaId() { return cuentaId; }
    public String getNombre() { return nombre; }
    public BigDecimal getSaldoFinal() { return saldoFinal; }
}
