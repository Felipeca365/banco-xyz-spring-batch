package cl.duoc.backendiii.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    private Long id;

    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;

    @Column(name = "es_anomalia")
    private Boolean esAnomalia;

    @Column(name = "motivo_anomalia")
    private String motivoAnomalia;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getEsAnomalia() { return esAnomalia; }
    public void setEsAnomalia(Boolean esAnomalia) { this.esAnomalia = esAnomalia; }

    public String getMotivoAnomalia() { return motivoAnomalia; }
    public void setMotivoAnomalia(String motivoAnomalia) { this.motivoAnomalia = motivoAnomalia; }
}