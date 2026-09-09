package cl.duoc.backendiii.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas_intereses")
public class CuentaInteres {

    @Id
    @Column(name = "cuenta_id")
    private Long cuentaId;

    private String nombre;

    @Column(name = "saldo_original")
    private BigDecimal saldoOriginal;

    @Column(name = "saldo_final")
    private BigDecimal saldoFinal;

    private Integer edad;
    private String tipo;

    @Column(name = "es_anomalia")
    private Boolean esAnomalia;

    @Column(name = "motivo_anomalia")
    private String motivoAnomalia;

    // Getters y setters
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getSaldoOriginal() { return saldoOriginal; }
    public void setSaldoOriginal(BigDecimal saldoOriginal) { this.saldoOriginal = saldoOriginal; }

    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getEsAnomalia() { return esAnomalia; }
    public void setEsAnomalia(Boolean esAnomalia) { this.esAnomalia = esAnomalia; }

    public String getMotivoAnomalia() { return motivoAnomalia; }
    public void setMotivoAnomalia(String motivoAnomalia) { this.motivoAnomalia = motivoAnomalia; }
}