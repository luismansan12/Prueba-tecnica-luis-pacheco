package com.pruebatecnica.polizas.repository;

import com.pruebatecnica.polizas.model.EstadoPoliza;
import com.pruebatecnica.polizas.model.Poliza;
import com.pruebatecnica.polizas.model.TipoPoliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    /**
     * Ambos filtros son opcionales: si llegan nulos no se aplican.
     */
    @Query("""
            select p from Poliza p
            where (:tipo is null or p.tipo = :tipo)
              and (:estado is null or p.estado = :estado)
            order by p.id
            """)
    List<Poliza> buscarPorTipoYEstado(@Param("tipo") TipoPoliza tipo, @Param("estado") EstadoPoliza estado);
}
