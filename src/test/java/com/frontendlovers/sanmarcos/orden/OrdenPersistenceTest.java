package com.frontendlovers.sanmarcos.orden;

import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.OrdenDetalle;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;
import com.frontendlovers.sanmarcos.repository.OrdenDetalleRepository;
import com.frontendlovers.sanmarcos.repository.OrdenRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

//Prueba que guarda una orden con su detalle
@SpringBootTest
@Transactional
class OrdenPersistenceTest {

    @Autowired OrdenRepository ordenRepository;
    @Autowired OrdenDetalleRepository ordenDetalleRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;

    private Usuario usuario;
    private Orden orden;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.save(Usuario.builder()
                .run("33.333.333-3")
                .email("orden." + System.currentTimeMillis() + "@test.com")
                .password("dummy-hash")
                .nombre("Orden")
                .apellido("Test")
                .rol(rolRepository.findByNombre("CLIENTE").orElseThrow())
                .build());

        Producto producto = productoRepository.findByCodigo("ALI-001").orElseThrow();

        BigDecimal precioConDescuento = producto.getPrecio()
                .multiply(BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(producto.getDescuento())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                ));
        BigDecimal subtotal = precioConDescuento.multiply(BigDecimal.valueOf(2));

        OrdenDetalle detalle = OrdenDetalle.builder()
                .producto(producto)
                .productoNombre(producto.getNombre())
                .precioUnitario(producto.getPrecio())
                .descuento(producto.getDescuento())
                .cantidad(2)
                .subtotal(subtotal)
                .build();

        orden = Orden.builder()
                .numeroOrden("ORD-TEST-" + System.currentTimeMillis())
                .usuario(usuario)
                .estado(EstadoOrden.PAGADA)
                .total(subtotal)
                .direccionEnvio("Av. Siempre Viva 123")
                .tipoEntrega(TipoEntrega.ENVIO)
                .contactoNombre(usuario.getNombre())
                .contactoApellido(usuario.getApellido())
                .contactoEmail(usuario.getEmail())
                .detalles(List.of(detalle))
                .build();

        detalle.setOrden(orden);
        orden = ordenRepository.save(orden);
    }

    @Test
    void ordenSeGuardaConDetalles() {
        assertThat(orden.getId()).isNotNull();
        assertThat(orden.getDetalles()).hasSize(1);
        assertThat(orden.getDetalles().get(0).getProductoNombre())
                .isEqualTo("Croquetas Premium Perro");
    }

    @Test
    void findByNumeroOrdenFunciona() {
        Optional<Orden> encontrada = ordenRepository.findByNumeroOrden(orden.getNumeroOrden());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTotal()).isEqualTo(orden.getTotal());
    }

    @Test
    void findByUsuarioIdDevuelveLaOrden() {
        List<Orden> ordenes = ordenRepository.findByUsuarioId(usuario.getId());
        assertThat(ordenes).hasSize(1);
        assertThat(ordenes.get(0).getNumeroOrden()).isEqualTo(orden.getNumeroOrden());
    }

    @Test
    void findByIdWithDetallesCargaItems() {
        Orden encontrada = ordenRepository.findByIdWithDetalles(orden.getId()).orElseThrow();
        assertThat(encontrada.getDetalles()).hasSize(1);
    }

    @Test
    void findByOrdenIdEnDetalleRepository() {
        List<OrdenDetalle> detalles = ordenDetalleRepository.findByOrdenId(orden.getId());
        assertThat(detalles).hasSize(1);
    }
}