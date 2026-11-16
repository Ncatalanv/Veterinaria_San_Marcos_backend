package com.frontendlovers.sanmarcos.orden;

import com.frontendlovers.sanmarcos.dto.OrdenItemRequest;
import com.frontendlovers.sanmarcos.dto.OrdenRequest;
import com.frontendlovers.sanmarcos.dto.OrdenResponse;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;
import com.frontendlovers.sanmarcos.exception.OrdenStateException;
import com.frontendlovers.sanmarcos.exception.StockInsuficienteException;
import com.frontendlovers.sanmarcos.repository.OrdenRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.service.OrdenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrdenServiceTest {

    @Autowired OrdenService ordenService;
    @Autowired ProductoRepository productoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired OrdenRepository ordenRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.save(Usuario.builder()
                .run("33.333.333-3")
                .email("orden.service." + System.currentTimeMillis() + "@test.com")
                .password("dummy")
                .nombre("Orden")
                .apellido("Service")
                .rol(rolRepository.findByNombre("CLIENTE").orElseThrow())
                .build());
    }

    private OrdenRequest request(int cantidad) {
        return new OrdenRequest(
                "Av. Siempre Viva 123",
                TipoEntrega.ENVIO,
                "Dejar en conserjería",
                "Orden",
                "Service",
                usuario.getEmail(),
                "+56912345678",
                List.of(new OrdenItemRequest(productoId(), cantidad))
        );
    }

    private Long productoId() {
        return productoRepository.findByCodigo("ALI-001").orElseThrow().getId();
    }

    @Test
    void crearOrdenDescuentaStockYCalculaTotal() {
        Producto antes = productoRepository.findByCodigo("ALI-001").orElseThrow();
        assertThat(antes.getStock()).isEqualTo(50);

        OrdenResponse response = ordenService.crearOrden(usuario.getId(), request(2));

        assertThat(response.numeroOrden()).startsWith("ORD-");
        assertThat(response.estado()).isEqualTo("PAGADA");
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("82782.00"));
        assertThat(response.detalles()).hasSize(1);
        assertThat(response.detalles().get(0).subtotal())
                .isEqualByComparingTo(new BigDecimal("82782.00"));

        Producto despues = productoRepository.findByCodigo("ALI-001").orElseThrow();
        assertThat(despues.getStock()).isEqualTo(48);
    }

    @Test
    void crearOrdenConStockInsuficienteLanzaExcepcionYNoDescuenta() {
        Producto antes = productoRepository.findByCodigo("ALI-001").orElseThrow();

        assertThatThrownBy(() -> ordenService.crearOrden(usuario.getId(), request(1000)))
                .isInstanceOf(StockInsuficienteException.class);

        Producto despues = productoRepository.findByCodigo("ALI-001").orElseThrow();
        assertThat(despues.getStock()).isEqualTo(antes.getStock());
    }

    @Test
    void cancelarOrdenRestauraStock() {
        OrdenResponse orden = ordenService.crearOrden(usuario.getId(), request(5));
        assertThat(productoRepository.findByCodigo("ALI-001").orElseThrow().getStock()).isEqualTo(45);

        OrdenResponse cancelada = ordenService.cancelarOrden(orden.id(), usuario.getId());

        assertThat(cancelada.estado()).isEqualTo("CANCELADA");
        assertThat(productoRepository.findByCodigo("ALI-001").orElseThrow().getStock()).isEqualTo(50);
    }

    @Test
    void clienteNoPuedeCancelarOrdenAjena() {
        Usuario otro = usuarioRepository.save(Usuario.builder()
                .run("44.444.444-4")
                .email("otro.orden." + System.currentTimeMillis() + "@test.com")
                .password("dummy")
                .nombre("Otro")
                .apellido("Usuario")
                .rol(rolRepository.findByNombre("CLIENTE").orElseThrow())
                .build());

        OrdenResponse orden = ordenService.crearOrden(usuario.getId(), request(1));

        assertThatThrownBy(() -> ordenService.cancelarOrden(orden.id(), otro.getId()))
                .isInstanceOf(OrdenStateException.class);
    }

    @Test
    void cambiarEstadoInvalidoLanzaExcepcion() {
        OrdenResponse orden = ordenService.crearOrden(usuario.getId(), request(1));

        assertThatThrownBy(() -> ordenService.cambiarEstado(orden.id(), EstadoOrden.ENTREGADA))
                .isInstanceOf(OrdenStateException.class);
    }

    @Test
    void cambiarEstadoValidoActualizaLaOrden() {
        OrdenResponse orden = ordenService.crearOrden(usuario.getId(), request(1));

        OrdenResponse actualizada = ordenService.cambiarEstado(orden.id(), EstadoOrden.EN_PREPARACION);

        assertThat(actualizada.estado()).isEqualTo("EN_PREPARACION");
    }
}
