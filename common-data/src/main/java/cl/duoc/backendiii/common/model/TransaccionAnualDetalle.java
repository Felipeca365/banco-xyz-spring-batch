package cl.duoc.backendiii.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones_anuales_detalle")
public class TransaccionAnualDetalle {

    @Id
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;

    @Column(name = "es_anomalia")
    private Boolean esAnomalia;

    @Column(name = "motivo_anomalia")
    private String motivoAnomalia;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTransaccion() { return transaccion; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getEsAnomalia() { return esAnomalia; }
    public void setEsAnomalia(Boolean esAnomalia) { this.esAnomalia = esAnomalia; }

    public String getMotivoAnomalia() { return motivoAnomalia; }
    public void setMotivoAnomalia(String motivoAnomalia) { this.motivoAnomalia = motivoAnomalia; }
}