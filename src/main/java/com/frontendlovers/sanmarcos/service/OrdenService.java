package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.OrdenDetalleResponse;
import com.frontendlovers.sanmarcos.dto.OrdenItemRequest;
import com.frontendlovers.sanmarcos.dto.OrdenRequest;
import com.frontendlovers.sanmarcos.dto.OrdenResponse;
import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.OrdenDetalle;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import com.frontendlovers.sanmarcos.exception.OrdenStateException;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.exception.StockInsuficienteException;
import com.frontendlovers.sanmarcos.repository.OrdenRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public OrdenService(OrdenRepository ordenRepository,
                        ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crea una orden descontando stock, calculando precios con descuento y generando un número único
    @Transactional
    public OrdenResponse crearOrden(Long usuarioId, OrdenRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Agrupa ítems repetidos del mismo producto sumando cantidades
        Map<Long, Integer> cantidadPorProducto = new LinkedHashMap<>();
        for (OrdenItemRequest item : request.items()) {
            cantidadPorProducto.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        List<OrdenDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cantidadPorProducto.entrySet()) {
            Long productoId = entry.getKey();
            int cantidad = entry.getValue();

            Producto producto = productoRepository.findByIdWithLock(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ResourceNotFoundException("Producto no disponible: " + producto.getNombre());
            }

            if (producto.getStock() < cantidad) {
                throw new StockInsuficienteException(
                        String.format("Stock insuficiente para %s. Disponible: %d, solicitado: %d",
                                producto.getNombre(), producto.getStock(), cantidad));
            }

            producto.setStock(producto.getStock() - cantidad);

            BigDecimal precioConDescuento = producto.getPrecio()
                    .multiply(BigDecimal.ONE.subtract(
                            BigDecimal.valueOf(producto.getDescuento())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    ));
            BigDecimal subtotal = precioConDescuento.multiply(BigDecimal.valueOf(cantidad));

            OrdenDetalle detalle = OrdenDetalle.builder()
                    .producto(producto)
                    .productoNombre(producto.getNombre())
                    .precioUnitario(producto.getPrecio())
                    .descuento(producto.getDescuento())
                    .cantidad(cantidad)
                    .subtotal(subtotal)
                    .build();

            detalles.add(detalle);
            total = total.add(subtotal);
        }

        Orden orden = Orden.builder()
                .numeroOrden(generarNumeroOrden())
                .usuario(usuario)
                .estado(EstadoOrden.PAGADA)
                .total(total)
                .direccionEnvio(request.direccionEnvio())
                .tipoEntrega(request.tipoEntrega())
                .instrucciones(request.instrucciones())
                .contactoNombre(request.contactoNombre())
                .contactoApellido(request.contactoApellido())
                .contactoEmail(request.contactoEmail())
                .contactoTelefono(request.contactoTelefono())
                .detalles(detalles)
                .build();

        for (OrdenDetalle detalle : detalles) {
            detalle.setOrden(orden);
        }

        return OrdenResponse.from(ordenRepository.save(orden));
    }

    // Cancela una orden propia restaurando el stock, solo si está PAGADA o EN_PREPARACION
    @Transactional
    public OrdenResponse cancelarOrden(Long ordenId, Long usuarioId) {
        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw new OrdenStateException("No puedes cancelar una orden que no te pertenece");
        }

        if (orden.getEstado() != EstadoOrden.PAGADA && orden.getEstado() != EstadoOrden.EN_PREPARACION) {
            throw new OrdenStateException("Solo se pueden cancelar órdenes pagadas o en preparación");
        }

        for (OrdenDetalle detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() + detalle.getCantidad());
            }
        }

        orden.setEstado(EstadoOrden.CANCELADA);
        return OrdenResponse.from(ordenRepository.save(orden));
    }

    // Cambia el estado de una orden validando las transiciones permitidas
    @Transactional
    public OrdenResponse cambiarEstado(Long ordenId, EstadoOrden nuevoEstado) {
        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (orden.getEstado() == nuevoEstado) {
            return OrdenResponse.from(orden);
        }

        boolean valido = switch (orden.getEstado()) {
            case PAGADA -> nuevoEstado == EstadoOrden.EN_PREPARACION || nuevoEstado == EstadoOrden.CANCELADA;
            case EN_PREPARACION -> nuevoEstado == EstadoOrden.ENVIADA || nuevoEstado == EstadoOrden.CANCELADA;
            case ENVIADA -> nuevoEstado == EstadoOrden.ENTREGADA || nuevoEstado == EstadoOrden.CANCELADA;
            default -> false;
        };

        if (!valido) {
            throw new OrdenStateException(
                    String.format("Transición de estado no permitida: %s -> %s", orden.getEstado(), nuevoEstado));
        }

        orden.setEstado(nuevoEstado);
        return OrdenResponse.from(ordenRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorId(Long id) {
        return OrdenResponse.from(ordenRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada")));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAll().stream()
                .map(OrdenResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorUsuario(Long usuarioId) {
        return ordenRepository.findByUsuarioId(usuarioId).stream()
                .map(OrdenResponse::from)
                .toList();
    }

    private String generarNumeroOrden() {
        long siguiente = ordenRepository.findTopByOrderByIdDesc()
                .map(o -> o.getId() + 1)
                .orElse(1L);
        return String.format("ORD-%04d", siguiente);
    }
}
