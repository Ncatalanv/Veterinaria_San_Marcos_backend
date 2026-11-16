package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.dashboard.DashboardMetricsResponse;
import com.frontendlovers.sanmarcos.dto.dashboard.ProductoStockCriticoItem;
import com.frontendlovers.sanmarcos.dto.dashboard.UltimaOrdenItem;
import com.frontendlovers.sanmarcos.dto.dashboard.UltimaSolicitudItem;
import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.SolicitudServicio;
import com.frontendlovers.sanmarcos.repository.OrdenRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.SolicitudServicioRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private static final int ULTIMOS = 5;

    private final OrdenRepository ordenRepository;
    private final SolicitudServicioRepository solicitudServicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public DashboardService(OrdenRepository ordenRepository,
                            SolicitudServicioRepository solicitudServicioRepository,
                            UsuarioRepository usuarioRepository,
                            ProductoRepository productoRepository) {
        this.ordenRepository = ordenRepository;
        this.solicitudServicioRepository = solicitudServicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getMetrics() {
        long totalOrdenes = ordenRepository.count();
        long totalSolicitudes = solicitudServicioRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long totalProductos = productoRepository.count();

        List<ProductoStockCriticoItem> stockCritico = productoRepository.findStockCritico().stream()
                .map(this::toStockCritico)
                .toList();

        List<UltimaOrdenItem> ultimasOrdenes = ordenRepository.findUltimas(PageRequest.of(0, ULTIMOS)).stream()
                .map(this::toUltimaOrden)
                .toList();

        List<UltimaSolicitudItem> ultimasSolicitudes = solicitudServicioRepository.findUltimas(PageRequest.of(0, ULTIMOS)).stream()
                .map(this::toUltimaSolicitud)
                .toList();

        return new DashboardMetricsResponse(
                totalOrdenes,
                totalSolicitudes,
                totalUsuarios,
                totalProductos,
                stockCritico,
                ultimasOrdenes,
                ultimasSolicitudes
        );
    }

    private ProductoStockCriticoItem toStockCritico(Producto p) {
        return new ProductoStockCriticoItem(p.getId(), p.getCodigo(), p.getNombre(), p.getStock(), p.getStockCritico());
    }

    private UltimaOrdenItem toUltimaOrden(Orden o) {
        String cliente = o.getUsuario().getNombre() + " " + o.getUsuario().getApellido();
        return new UltimaOrdenItem(o.getId(), o.getNumeroOrden(), o.getCreatedAt(), o.getTotal(), o.getEstado().name(), cliente);
    }

    private UltimaSolicitudItem toUltimaSolicitud(SolicitudServicio s) {
        String cliente = s.getUsuario().getNombre() + " " + s.getUsuario().getApellido();
        return new UltimaSolicitudItem(s.getId(), s.getServicio().getNombre(), s.getFechaSolicitada(), s.getEstado().name(), cliente);
    }
}
