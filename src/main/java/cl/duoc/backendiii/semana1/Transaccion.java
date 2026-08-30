package cl.duoc.backendiii.semana1;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaccion {

    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;

    // constructor vacio; Spring Batch lo necesita para poder crear el objeto
    // antes de rellenarlo campo por campo con los datos del CSV.
    public Transaccion() {

    }

    // Getters y Setters: Spring Batch los usa internamente para "inyectar" 
    // cada valor leido del CSV en el campo correspondiente.
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

    @Override
    public String toString() {
        return "Transaccion{id=" + id + ", fecha=" + fecha + ", monto=" + monto + ", tipo=" + tipo + " '}";
    }
}
