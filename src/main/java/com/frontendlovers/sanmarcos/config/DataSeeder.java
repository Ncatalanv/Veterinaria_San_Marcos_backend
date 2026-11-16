package com.frontendlovers.sanmarcos.config;

import com.frontendlovers.sanmarcos.entity.Categoria;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.Servicio;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

//Carga datos iniciales de productos y servicios para que el catálogo esté completo al arrancar
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;

    public DataSeeder(CategoriaRepository categoriaRepository,
                      ProductoRepository productoRepository,
                      ServicioRepository servicioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    public void run(String... args) {
        seedCategorias();
        seedProductos();
        seedServicios();
    }

    private void seedCategorias() {
        String[][] productos = {
                {"Antibióticos", "Medicamentos antibióticos para mascotas"},
                {"Antiparasitarios", "Productos contra parásitos internos y externos"},
                {"Antiinflamatorios", "Medicamentos antiinflamatorios y analgésicos"},
                {"Dermatología", "Productos para el cuidado de la piel"},
                {"Digestivo", "Productos para el sistema digestivo"},
                {"Cardíaco", "Medicamentos para el corazón"},
                {"Analgésicos", "Medicamentos para el dolor"},
                {"Vacunas", "Vacunas para perros y gatos"},
                {"Suplementos", "Vitaminas y suplementos"},
        };

        for (String[] c : productos) {
            crearCategoriaSiNoExiste(c[0], TipoCategoria.PRODUCTO, c[1]);
        }

        String[][] servicios = {
                {"Consultas", "Consultas veterinarias generales y especializadas"},
                {"Vacunación", "Vacunación para perros y gatos"},
                {"Cirugía", "Procedimientos quirúrgicos veterinarios"},
                {"Desparasitación", "Tratamientos antiparasitarios"},
                {"Exámenes", "Exámenes de laboratorio e imagenología"},
                {"Otros", "Otros servicios veterinarios"},
        };

        for (String[] c : servicios) {
            crearCategoriaSiNoExiste(c[0], TipoCategoria.SERVICIO, c[1]);
        }
    }

    private void crearCategoriaSiNoExiste(String nombre, TipoCategoria tipo, String descripcion) {
        categoriaRepository.findByNombreAndTipo(nombre, tipo)
                .orElseGet(() -> {
                    Categoria c = Categoria.builder()
                            .nombre(nombre)
                            .tipo(tipo)
                            .descripcion(descripcion)
                            .build();
                    return categoriaRepository.save(c);
                });
    }

    private Categoria categoria(String nombre, TipoCategoria tipo) {
        return categoriaRepository.findByNombreAndTipo(nombre, tipo)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada: " + nombre));
    }

    private void seedProductos() {
        if (productoRepository.count() >= 22) {
            return;
        }

        Map<String, Producto> productos = Map.ofEntries(
                Map.entry("ME001", Producto.builder()
                        .codigo("ME001").nombre("Amoxibay 250mg")
                        .descripcion("Producto veterinario: Amoxibay 250mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("4200")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/amoxilina.png")
                        .categoria(categoria("Antibióticos", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME002", Producto.builder()
                        .codigo("ME002").nombre("Enrox 50mg")
                        .descripcion("Producto veterinario: Enrox 50mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("6800")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/enrox.png")
                        .categoria(categoria("Antibióticos", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME003", Producto.builder()
                        .codigo("ME003").nombre("Metrobay 250mg")
                        .descripcion("Producto veterinario: Metrobay 250mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("3900")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/metrocare.png")
                        .categoria(categoria("Antibióticos", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME004", Producto.builder()
                        .codigo("ME004").nombre("Nexgard")
                        .descripcion("Producto veterinario: Nexgard.")
                        .presentacion("Masticable 1 unid.")
                        .precio(new BigDecimal("9500")).descuento(20).stock(28).stockCritico(5)
                        .imagen("images/nexgard.png")
                        .categoria(categoria("Antiparasitarios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME005", Producto.builder()
                        .codigo("ME005").nombre("Bravecto")
                        .descripcion("Producto veterinario: Bravecto.")
                        .presentacion("Masticable 1 unid.")
                        .precio(new BigDecimal("18900")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/bravecto.png")
                        .categoria(categoria("Antiparasitarios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME006", Producto.builder()
                        .codigo("ME006").nombre("Revolution Plus")
                        .descripcion("Producto veterinario: Revolution Plus.")
                        .presentacion("Pipeta 1 unid.")
                        .precio(new BigDecimal("14500")).descuento(15).stock(28).stockCritico(5)
                        .imagen("images/Revolution.png")
                        .categoria(categoria("Antiparasitarios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME007", Producto.builder()
                        .codigo("ME007").nombre("Drontal Plus")
                        .descripcion("Producto veterinario: Drontal Plus.")
                        .presentacion("Comprimido 1 unid.")
                        .precio(new BigDecimal("3200")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/drontal.png")
                        .categoria(categoria("Antiparasitarios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME008", Producto.builder()
                        .codigo("ME008").nombre("Milbemax Gato")
                        .descripcion("Producto veterinario: Milbemax Gato.")
                        .presentacion("Comprimido 2 unid.")
                        .precio(new BigDecimal("6800")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/milbemax.png")
                        .categoria(categoria("Antiparasitarios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME009", Producto.builder()
                        .codigo("ME009").nombre("Meloxicam 1mg")
                        .descripcion("Producto veterinario: Meloxicam 1mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("4500")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/meloxivet.png")
                        .categoria(categoria("Antiinflamatorios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME010", Producto.builder()
                        .codigo("ME010").nombre("Carprofen 50mg")
                        .descripcion("Producto veterinario: Carprofen 50mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("9800")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/Carprofelican.png")
                        .categoria(categoria("Antiinflamatorios", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME011", Producto.builder()
                        .codigo("ME011").nombre("Clorhexidina shampoo")
                        .descripcion("Producto veterinario: Clorhexidina shampoo.")
                        .presentacion("Frasco 250ml")
                        .precio(new BigDecimal("8900")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/champo.png")
                        .categoria(categoria("Dermatología", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME012", Producto.builder()
                        .codigo("ME012").nombre("Malaseb shampoo")
                        .descripcion("Producto veterinario: Malaseb shampoo.")
                        .presentacion("Frasco 250ml")
                        .precio(new BigDecimal("12500")).descuento(15).stock(28).stockCritico(5)
                        .imagen("images/malaseb.png")
                        .categoria(categoria("Dermatología", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME013", Producto.builder()
                        .codigo("ME013").nombre("Apoquel 16mg")
                        .descripcion("Producto veterinario: Apoquel 16mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("22000")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/apoquel.png")
                        .categoria(categoria("Dermatología", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME014", Producto.builder()
                        .codigo("ME014").nombre("Probifor")
                        .descripcion("Producto veterinario: Probifor.")
                        .presentacion("Sobre 5ml x10")
                        .precio(new BigDecimal("5600")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/probifor.png")
                        .categoria(categoria("Digestivo", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME015", Producto.builder()
                        .codigo("ME015").nombre("Omeprazol 10mg vet")
                        .descripcion("Producto veterinario: Omeprazol 10mg vet.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("3800")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/omeprazol.pmg.jpg")
                        .categoria(categoria("Digestivo", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME016", Producto.builder()
                        .codigo("ME016").nombre("Vetmedin 2.5mg")
                        .descripcion("Producto veterinario: Vetmedin 2.5mg.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("28000")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/vetmedin.png")
                        .categoria(categoria("Cardíaco", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME017", Producto.builder()
                        .codigo("ME017").nombre("Tramadol 50mg vet")
                        .descripcion("Producto veterinario: Tramadol 50mg vet.")
                        .presentacion("Blíster 10 comp.")
                        .precio(new BigDecimal("5200")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/tramadol.png")
                        .categoria(categoria("Analgésicos", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME018", Producto.builder()
                        .codigo("ME018").nombre("Nobivac DHPPi")
                        .descripcion("Producto veterinario: Nobivac DHPPi.")
                        .presentacion("Vial 1 dosis")
                        .precio(new BigDecimal("8500")).descuento(10).stock(28).stockCritico(5)
                        .imagen("images/Nobivac.png")
                        .categoria(categoria("Vacunas", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME019", Producto.builder()
                        .codigo("ME019").nombre("Nobivac Rabies")
                        .descripcion("Producto veterinario: Nobivac Rabies.")
                        .presentacion("Vial 1 dosis")
                        .precio(new BigDecimal("5800")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/novibac.png")
                        .categoria(categoria("Vacunas", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME020", Producto.builder()
                        .codigo("ME020").nombre("Felocell CVR")
                        .descripcion("Producto veterinario: Felocell CVR.")
                        .presentacion("Vial 1 dosis")
                        .precio(new BigDecimal("7200")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/Felocell.png")
                        .categoria(categoria("Vacunas", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME021", Producto.builder()
                        .codigo("ME021").nombre("Omega vet 3-6-9")
                        .descripcion("Producto veterinario: Omega vet 3-6-9.")
                        .presentacion("Frasco 100ml")
                        .precio(new BigDecimal("9900")).descuento(25).stock(28).stockCritico(5)
                        .imagen("images/omega.png")
                        .categoria(categoria("Suplementos", TipoCategoria.PRODUCTO))
                        .activo(true).build()),
                Map.entry("ME022", Producto.builder()
                        .codigo("ME022").nombre("Condrovet forte")
                        .descripcion("Producto veterinario: Condrovet forte.")
                        .presentacion("Blíster 30 comp.")
                        .precio(new BigDecimal("14500")).descuento(0).stock(28).stockCritico(5)
                        .imagen("images/condro.png")
                        .categoria(categoria("Suplementos", TipoCategoria.PRODUCTO))
                        .activo(true).build())
        );

        productos.values().forEach(p -> {
            if (!productoRepository.existsByCodigo(p.getCodigo())) {
                productoRepository.save(p);
            }
        });
    }

    private void seedServicios() {
        if (servicioRepository.count() >= 31) {
            return;
        }

        Map<String, Servicio> servicios = Map.ofEntries(
                Map.entry("SERV001", Servicio.builder()
                        .codigo("SERV001").nombre("Consulta general")
                        .descripcion("Servicio veterinario: Consulta general.")
                        .especie("Perro / Gato").duracion("30 min")
                        .precio(new BigDecimal("15000")).imagen("images/veterinario_gato.png")
                        .nota("").categoria(categoria("Consultas", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV002", Servicio.builder()
                        .codigo("SERV002").nombre("Consulta urgencia")
                        .descripcion("Servicio veterinario: Consulta urgencia.")
                        .especie("Perro / Gato").duracion("30 min")
                        .precio(new BigDecimal("25000")).imagen("images/consulta_emergencia.png")
                        .nota("Fuera de horario +$10.000").categoria(categoria("Consultas", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV003", Servicio.builder()
                        .codigo("SERV003").nombre("Control postoperatorio")
                        .descripcion("Servicio veterinario: Control postoperatorio.")
                        .especie("Perro / Gato").duracion("20 min")
                        .precio(new BigDecimal("10000")).imagen("images/post_operatorio.png")
                        .nota("").categoria(categoria("Consultas", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV004", Servicio.builder()
                        .codigo("SERV004").nombre("Consulta ave / conejo")
                        .descripcion("Servicio veterinario: Consulta ave / conejo.")
                        .especie("Ave / Conejo").duracion("30 min")
                        .precio(new BigDecimal("18000")).imagen("images/ave_conejo.png")
                        .nota("").categoria(categoria("Consultas", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV005", Servicio.builder()
                        .codigo("SERV005").nombre("Segunda opinión médica")
                        .descripcion("Servicio veterinario: Segunda opinión médica.")
                        .especie("Todas").duracion("40 min")
                        .precio(new BigDecimal("20000")).imagen("images/segunda_opcion.png")
                        .nota("Requiere ficha previa").categoria(categoria("Consultas", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV006", Servicio.builder()
                        .codigo("SERV006").nombre("Vacuna antirrábica canina")
                        .descripcion("Servicio veterinario: Vacuna antirrábica canina.")
                        .especie("Perro").duracion("10 min")
                        .precio(new BigDecimal("12000")).imagen("images/vacuna_perro.png")
                        .nota("Obligatoria por ley").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV007", Servicio.builder()
                        .codigo("SERV007").nombre("Vacuna sextuple canina")
                        .descripcion("Servicio veterinario: Vacuna sextuple canina.")
                        .especie("Perro").duracion("10 min")
                        .precio(new BigDecimal("18000")).imagen("images/vacuna_sextuple.png")
                        .nota("Refuerzo anual").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV008", Servicio.builder()
                        .codigo("SERV008").nombre("Vacuna bivalente felina")
                        .descripcion("Servicio veterinario: Vacuna bivalente felina.")
                        .especie("Gato").duracion("10 min")
                        .precio(new BigDecimal("15000")).imagen("images/vacuna_felina.png")
                        .nota("Refuerzo anual").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV009", Servicio.builder()
                        .codigo("SERV009").nombre("Vacuna triple felina")
                        .descripcion("Servicio veterinario: Vacuna triple felina.")
                        .especie("Gato").duracion("10 min")
                        .precio(new BigDecimal("17000")).imagen("images/vacuna_triple_felina.png")
                        .nota("Refuerzo anual").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV010", Servicio.builder()
                        .codigo("SERV010").nombre("Vacuna Bordetella canina")
                        .descripcion("Servicio veterinario: Vacuna Bordetella canina.")
                        .especie("Perro").duracion("10 min")
                        .precio(new BigDecimal("14000")).imagen("images/vacuna_bordetella.png")
                        .nota("Tos de las perreras").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV011", Servicio.builder()
                        .codigo("SERV011").nombre("Vacuna antirrábica felina")
                        .descripcion("Servicio veterinario: Vacuna antirrábica felina.")
                        .especie("Gato").duracion("10 min")
                        .precio(new BigDecimal("12000")).imagen("images/vacuna_antirrabica_felina.png")
                        .nota("").categoria(categoria("Vacunación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV012", Servicio.builder()
                        .codigo("SERV012").nombre("Esterilización hembra canina")
                        .descripcion("Servicio veterinario: Esterilización hembra canina.")
                        .especie("Perra").duracion("90 min")
                        .precio(new BigDecimal("80000")).imagen("images/esterilizacion_canina.png")
                        .nota("Incluye anestesia y hospitalización 24h").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV013", Servicio.builder()
                        .codigo("SERV013").nombre("Esterilización macho canino")
                        .descripcion("Servicio veterinario: Esterilización macho canino.")
                        .especie("Perro").duracion("60 min")
                        .precio(new BigDecimal("60000")).imagen("images/esterilizacion_canino.png")
                        .nota("Incluye anestesia").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV014", Servicio.builder()
                        .codigo("SERV014").nombre("Esterilización hembra felina")
                        .descripcion("Servicio veterinario: Esterilización hembra felina.")
                        .especie("Gata").duracion("60 min")
                        .precio(new BigDecimal("65000")).imagen("images/esterilizacion_felinaa.png")
                        .nota("Incluye anestesia y hospitalización 24h").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV015", Servicio.builder()
                        .codigo("SERV015").nombre("Esterilización macho felino")
                        .descripcion("Servicio veterinario: Esterilización macho felino.")
                        .especie("Gato").duracion("65 min")
                        .precio(new BigDecimal("50000")).imagen("images/esterilizacion_felino.png")
                        .nota("Incluye anestesia").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV016", Servicio.builder()
                        .codigo("SERV016").nombre("Extirpación de tumor cutáneo")
                        .descripcion("Servicio veterinario: Extirpación de tumor cutáneo.")
                        .especie("Perro / Gato").duracion("60 min")
                        .precio(new BigDecimal("120000")).imagen("images/cirugia.png")
                        .nota("Precio referencial").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV017", Servicio.builder()
                        .codigo("SERV017").nombre("Cesárea de urgencia")
                        .descripcion("Servicio veterinario: Cesárea de urgencia.")
                        .especie("Perra / Gata").duracion("120 min")
                        .precio(new BigDecimal("180000")).imagen("images/embarazaso_canina.png")
                        .nota("").categoria(categoria("Cirugía", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV018", Servicio.builder()
                        .codigo("SERV018").nombre("Desparasitación interna pequeños")
                        .descripcion("Servicio veterinario: Desparasitación interna pequeños.")
                        .especie("Perro").duracion("5 min")
                        .precio(new BigDecimal("8000")).imagen("images/desparacitacion_perro_pequeño.png")
                        .nota("< 10 kg").categoria(categoria("Desparasitación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV019", Servicio.builder()
                        .codigo("SERV019").nombre("Desparasitación interna medianos")
                        .descripcion("Servicio veterinario: Desparasitación interna medianos.")
                        .especie("Perro").duracion("5 min")
                        .precio(new BigDecimal("9500")).imagen("images/desparacitacion_perro_cachorro.png")
                        .nota("10-25 kg").categoria(categoria("Desparasitación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV020", Servicio.builder()
                        .codigo("SERV020").nombre("Desparasitación interna grandes")
                        .descripcion("Servicio veterinario: Desparasitación interna grandes.")
                        .especie("Perro").duracion("5 min")
                        .precio(new BigDecimal("11000")).imagen("images/desparacitacion_perro_grande.png")
                        .nota("> 25 kg").categoria(categoria("Desparasitación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV021", Servicio.builder()
                        .codigo("SERV021").nombre("Desparasitación interna felina")
                        .descripcion("Servicio veterinario: Desparasitación interna felina.")
                        .especie("Gato").duracion("5 min")
                        .precio(new BigDecimal("8000")).imagen("images/desparacitacion_felina.png")
                        .nota("").categoria(categoria("Desparasitación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV022", Servicio.builder()
                        .codigo("SERV022").nombre("Antiparasitario externo (pipeta)")
                        .descripcion("Servicio veterinario: Antiparasitario externo (pipeta).")
                        .especie("Perro / Gato").duracion("5 min")
                        .precio(new BigDecimal("7500")).imagen("images/antiparasitario.png")
                        .nota("Incluye aplicación").categoria(categoria("Desparasitación", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV023", Servicio.builder()
                        .codigo("SERV023").nombre("Hemograma completo")
                        .descripcion("Servicio veterinario: Hemograma completo.")
                        .especie("Perro / Gato").duracion("30 min")
                        .precio(new BigDecimal("22000")).imagen("images/hemograma.png")
                        .nota("Resultado en 24-48h").categoria(categoria("Exámenes", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV024", Servicio.builder()
                        .codigo("SERV024").nombre("Perfil bioquímico completo")
                        .descripcion("Servicio veterinario: Perfil bioquímico completo.")
                        .especie("Perro / Gato").duracion("30 min")
                        .precio(new BigDecimal("35000")).imagen("images/perfil_biomico.png")
                        .nota("Resultado en 24-48h").categoria(categoria("Exámenes", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV025", Servicio.builder()
                        .codigo("SERV025").nombre("Radiografía (1 proyección)")
                        .descripcion("Servicio veterinario: Radiografía (1 proyección).")
                        .especie("Perro / Gato").duracion("20 min")
                        .precio(new BigDecimal("28000")).imagen("images/radiografia.png")
                        .nota("").categoria(categoria("Exámenes", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV026", Servicio.builder()
                        .codigo("SERV026").nombre("Ecografía abdominal")
                        .descripcion("Servicio veterinario: Ecografía abdominal.")
                        .especie("Perro / Gato").duracion("30 min")
                        .precio(new BigDecimal("45000")).imagen("images/ecografia.png")
                        .nota("").categoria(categoria("Exámenes", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV027", Servicio.builder()
                        .codigo("SERV027").nombre("Test de leishmaniasis")
                        .descripcion("Servicio veterinario: Test de leishmaniasis.")
                        .especie("Perro").duracion("20 min")
                        .precio(new BigDecimal("18000")).imagen("images/test_leishmaniasis.png")
                        .nota("").categoria(categoria("Exámenes", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV028", Servicio.builder()
                        .codigo("SERV028").nombre("Corte de uñas")
                        .descripcion("Servicio veterinario: Corte de uñas.")
                        .especie("Perro / Gato").duracion("15 min")
                        .precio(new BigDecimal("5000")).imagen("images/corte_uñas.png")
                        .nota("").categoria(categoria("Otros", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV029", Servicio.builder()
                        .codigo("SERV029").nombre("Limpieza dental")
                        .descripcion("Servicio veterinario: Limpieza dental.")
                        .especie("Perro / Gato").duracion("45 min")
                        .precio(new BigDecimal("55000")).imagen("images/limpieza_dental.png")
                        .nota("Requiere anestesia").categoria(categoria("Otros", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV030", Servicio.builder()
                        .codigo("SERV030").nombre("Microchip identificación")
                        .descripcion("Servicio veterinario: Microchip identificación.")
                        .especie("Perro / Gato").duracion("10 min")
                        .precio(new BigDecimal("15000")).imagen("images/microchip.png")
                        .nota("Incluye registro").categoria(categoria("Otros", TipoCategoria.SERVICIO))
                        .activo(true).build()),
                Map.entry("SERV031", Servicio.builder()
                        .codigo("SERV031").nombre("Hospitalización (por día)")
                        .descripcion("Servicio veterinario: Hospitalización (por día).")
                        .especie("Perro / Gato").duracion("24 hrs")
                        .precio(new BigDecimal("30000")).imagen("images/hospitalizacion.png")
                        .nota("Incluye monitoreo y alimentación básica").categoria(categoria("Otros", TipoCategoria.SERVICIO))
                        .activo(true).build())
        );

        servicios.values().forEach(s -> {
            if (!servicioRepository.existsByCodigo(s.getCodigo())) {
                servicioRepository.save(s);
            }
        });
    }
}
