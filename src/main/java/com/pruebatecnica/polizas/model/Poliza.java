package com.pruebatecnica.polizas.model;

import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Póliza de arrendamiento. Las reglas de negocio viven aquí para que no se
 * puedan saltar desde ninguna otra capa.
 */
@Entity
@Table(name = "poliza")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Column(nullable = false)
    private String tomador;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private int mesesVigencia;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL)
    private List<Riesgo> riesgos = new ArrayList<>();

    protected Poliza() {
        // requerido por JPA
    }

    public Poliza(String numero, TipoPoliza tipo, String tomador, LocalDate fechaInicio,
                  int mesesVigencia, BigDecimal canonMensual) {
        this.numero = numero;
        this.tipo = tipo;
        this.tomador = tomador;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia);
        this.mesesVigencia = mesesVigencia;
        this.canonMensual = canonMensual;
        this.prima = calcularPrima();
        this.estado = EstadoPoliza.ACTIVA;
    }

    /**
     * Renueva la póliza por el mismo periodo de vigencia inicial, ajustando el canon
     * con el IPC. La prima se recalcula sobre el canon ya ajustado.
     */
    public void renovar(BigDecimal ipc) {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada");
        }
        canonMensual = canonMensual.multiply(BigDecimal.ONE.add(ipc)).setScale(0, RoundingMode.HALF_UP);
        prima = calcularPrima();
        fechaInicio = fechaFin;
        fechaFin = fechaFin.plusMonths(mesesVigencia);
        estado = EstadoPoliza.RENOVADA;
    }

    /**
     * Cancela la póliza y todos sus riesgos que sigan activos.
     */
    public void cancelar() {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("La póliza ya está cancelada");
        }
        estado = EstadoPoliza.CANCELADA;
        riesgos.stream()
                .filter(Riesgo::estaActivo)
                .forEach(Riesgo::cancelarPorCancelacionDePoliza);
    }

    /**
     * Solo las pólizas colectivas aceptan nuevos riesgos; así se garantiza también
     * que una póliza individual tenga un único riesgo.
     */
    public void agregarRiesgo(Riesgo riesgo) {
        if (tipo != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException(
                    "Solo se pueden agregar riesgos a pólizas colectivas; una póliza individual tiene un único riesgo");
        }
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se pueden agregar riesgos a una póliza cancelada");
        }
        vincularRiesgo(riesgo);
    }

    void vincularRiesgo(Riesgo riesgo) {
        riesgo.asignarPoliza(this);
        riesgos.add(riesgo);
    }

    private BigDecimal calcularPrima() {
        return canonMensual.multiply(BigDecimal.valueOf(mesesVigencia));
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public EstadoPoliza getEstado() {
        return estado;
    }

    public String getTomador() {
        return tomador;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getMesesVigencia() {
        return mesesVigencia;
    }

    public BigDecimal getCanonMensual() {
        return canonMensual;
    }

    public BigDecimal getPrima() {
        return prima;
    }

    public List<Riesgo> getRiesgos() {
        return Collections.unmodifiableList(riesgos);
    }
}
