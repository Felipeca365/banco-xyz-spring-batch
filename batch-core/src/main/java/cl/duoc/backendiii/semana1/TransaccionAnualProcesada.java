package cl.duoc.backendiii.semana1;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de salida del Processor para el Job 3.
 *
 * Aquí SÍ usamos LocalDate y BigDecimal, porque a esta altura
 * el Processor ya intentó convertir los datos crudos: si lo logró,
 * quedan bien tipados; si no, quedan en null y esAnomalia=true
 * explica por qué. Esta es la clase que el Writer inserta en la
 * tabla transacciones_anuales_detalle.
 */
public class TransaccionAnualProcesada {

    private Long cuentaId;
    private LocalDate fecha;       // null si no se pudo parsear
    private String transaccion;    // ya normalizado: deposito/retiro/compra/pago
    private BigDecimal monto;      // null si venía vacío
    private String descripcion;
    private boolean esAnomalia;
    private String motivoAnomalia;

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
}   