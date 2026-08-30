package cl.duoc.backendiii.semana1;

import java.math.BigDecimal;

public class CuentaInteresProcesada {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldoOriginal;
    private BigDecimal saldoFinal;
    private Integer edad;
    private String tipo;
    private boolean esAnomalia;
    private String motivoAnomalia;

    public CuentaInteresProcesada() {
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getSaldoOriginal() {
        return saldoOriginal;
    }

    public void setSaldoOriginal(BigDecimal saldoOriginal) {
        this.saldoOriginal = saldoOriginal;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
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
        return "CuentaInteresProcesada{cuentaId=" + cuentaId + ", nombre='" + nombre
                + "', saldoOriginal=" + saldoOriginal + ", saldoFinal=" + saldoFinal
                + ", edad=" + edad + ", tipo='" + tipo + "', esAnomalia=" + esAnomalia
                + ", motivoAnomalia='" + motivoAnomalia + "'}";
    }
}