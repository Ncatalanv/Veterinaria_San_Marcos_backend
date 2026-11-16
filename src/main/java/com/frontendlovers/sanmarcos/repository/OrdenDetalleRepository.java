package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.OrdenDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Repositorio del detalle, para buscar ítems por orden
public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, Long> {

    List<OrdenDetalle> findByOrdenId(Long ordenId);
}