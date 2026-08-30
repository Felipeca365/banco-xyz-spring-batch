package cl.duoc.backendiii.semana1;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaccionProcesada {

    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
    private boolean esAnomalia;
    private String motivoAnomalia;

    public TransaccionProcesada() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isEsAnomalia() {
        return esAnomalia;
    }

    public void setEsAnomalia(boolean esAnomalia) {
        this.esAnomalia = esAnomalia;
    }

    public String getMotivoAnomalia() {
        return motivoAnomalia;
    }

    public void setMotivoAnomalia(String motivoAnomalia) {
        this.motivoAnomalia = motivoAnomalia;
    }

    @Override
    public String toString() {
        return "TransaccionProcesada{id=" + id + ", fecha=" + fecha + ", monto=" + monto
                + ", tipo='" + tipo + "', esAnomalia=" + esAnomalia
                + ", motivoAnomalia='" + motivoAnomalia + "'}";
    }
}