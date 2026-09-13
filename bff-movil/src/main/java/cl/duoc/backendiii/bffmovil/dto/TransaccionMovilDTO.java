package cl.duoc.backendiii.bffmovil.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaccionMovilDTO {

    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;

    public TransaccionMovilDTO(Long id, LocalDate fecha, BigDecimal monto, String tipo) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public String getTipo() { return tipo; }
}
