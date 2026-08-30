package cl.duoc.backendiii.semana1;

/**
 * Modelo de entrada para el Job 3 (Estados de Cuenta Anuales).
 *
 * A diferencia de Transaccion.java (Job 1), aquí TODOS los campos
 * se guardan como String tal cual vienen del CSV. La conversión a
 * LocalDate/BigDecimal se hace en el Processor, no en el Reader,
 * porque el archivo semana_3 trae fechas en 4 formatos distintos
 * y montos vacíos: si el Reader intentara convertir directamente,
 * el Job se caería en la primera fila mala, antes de que el
 * Processor pueda marcarla como anomalía y seguir procesando.
 */
public class TransaccionAnual {

    private Long cuentaId;
    private String fecha;       // crudo, sin parsear todavía
    private String transaccion; // "deposito", "depósito", "retiro", "compra", "pago", etc.
    private String monto;       // crudo, puede venir vacío
    private String descripcion;

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public String getMonto() {
        return monto;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}