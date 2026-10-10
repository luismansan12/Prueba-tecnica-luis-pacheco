package com.pruebatecnica.polizas.model;

import com.pruebatecnica.polizas.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Riesgo asegurado: el inmueble arrendado, con su arrendatario (asegurado)
 * y su arrendador (beneficiario).
 */
@Entity
@Table(name = "riesgo")
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String direccionInmueble;

    @Column(nullable = false)
    private String arrendatario;

    @Column(nullable = false)
    private String arrendador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    protected Riesgo() {
        // requerido por JPA
    }

    public Riesgo(String direccionInmueble, String arrendatario, String arrendador) {
        this.direccionInmueble = direccionInmueble;
        this.arrendatario = arrendatario;
        this.arrendador = arrendador;
        this.estado = EstadoRiesgo.ACTIVO;
    }

    /**
     * Cancela el riesgo de forma individual. En una póliza individual el riesgo no se
     * cancela por separado: se cancela la póliza, para no dejarla vigente sin riesgo.
     */
    public void cancelar() {
        if (!estaActivo()) {
            throw new ReglaNegocioException("El riesgo ya está cancelado");
        }
        if (poliza.getTipo() == TipoPoliza.INDIVIDUAL) {
            throw new ReglaNegocioException(
                    "El riesgo de una póliza individual no se cancela por separado; se debe cancelar la póliza");
        }
        estado = EstadoRiesgo.CANCELADO;
    }

    void cancelarPorCancelacionDePoliza() {
        estado = EstadoRiesgo.CANCELADO;
    }

    void asignarPoliza(Poliza poliza) {
        this.poliza = poliza;
    }

    public boolean estaActivo() {
        return estado == EstadoRiesgo.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public String getDireccionInmueble() {
        return direccionInmueble;
    }

    public String getArrendatario() {
        return arrendatario;
    }

    public String getArrendador() {
        return arrendador;
    }

    public EstadoRiesgo getEstado() {
        return estado;
    }

    public Poliza getPoliza() {
        return poliza;
    }
}
