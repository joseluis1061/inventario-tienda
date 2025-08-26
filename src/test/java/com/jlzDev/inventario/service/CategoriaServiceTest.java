package com.jlzDev.inventario.service;

import com.jlzDev.inventario.entity.Categoria;
import com.jlzDev.inventario.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CategoriaService
 *
 * Cobertura de pruebas:
 * - Métodos de consulta (obtener, buscar)
 * - Operaciones CRUD (crear, actualizar, eliminar)
 * - Validaciones de negocio
 * - Manejo de errores y excepciones
 * - Casos edge (valores nulos, vacíos, límites)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService - Pruebas Unitarias")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    // Datos de prueba
    private Categoria categoriaElectronicos;
    private Categoria categoriaRopa;
    private Categoria categoriaLibros;

    @BeforeEach
    void setUp() {
        categoriaElectronicos = Categoria.builder()
                .id(1L)
                .nombre("Electrónicos")
                .descripcion("Dispositivos electrónicos y tecnología")
                .fechaCreacion(LocalDateTime.now())
                .build();

        categoriaRopa = Categoria.builder()
                .id(2L)
                .nombre("Ropa")
                .descripcion("Prendas de vestir y accesorios")
                .fechaCreacion(LocalDateTime.now())
                .build();

        categoriaLibros = Categoria.builder()
                .id(3L)
                .nombre("Libros")
                .descripcion("Literatura y material educativo")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    @Nested
    @DisplayName("Métodos de Consulta")
    class MetodosConsulta {

        @Test
        @DisplayName("obtenerTodas() - Debería devolver todas las categorías")
        void obtenerTodas_DeberiaRetornarTodasLasCategorias() {
            // Arrange
            List<Categoria> categoriasEsperadas = Arrays.asList(
                    categoriaElectronicos, categoriaRopa, categoriaLibros
            );
            when(categoriaRepository.findAll()).thenReturn(categoriasEsperadas);

            // Act
            List<Categoria> resultado = categoriaService.obtenerTodas();

            // Assert
            assertThat(resultado)
                    .hasSize(3)
                    .containsExactlyInAnyOrder(categoriaElectronicos, categoriaRopa, categoriaLibros);
            verify(categoriaRepository).findAll();
        }

        @Test
        @DisplayName("obtenerTodas() - Debería devolver lista vacía si no hay categorías")
        void obtenerTodas_DeberiaRetornarListaVaciaSiNoHayCategorias() {
            // Arrange
            when(categoriaRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Categoria> resultado = categoriaService.obtenerTodas();

            // Assert
            assertThat(resultado).isEmpty();
            verify(categoriaRepository).findAll();
        }

        @Test
        @DisplayName("obtenerPorId() - Debería devolver categoría cuando existe")
        void obtenerPorId_DeberiaRetornarCategoriaExistente() {
            // Arrange
            Long id = 1L;
            when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoriaElectronicos));

            // Act
            Optional<Categoria> resultado = categoriaService.obtenerPorId(id);

            // Assert
            assertThat(resultado)
                    .isPresent()
                    .contains(categoriaElectronicos);
            verify(categoriaRepository).findById(id);
        }

        @Test
        @DisplayName("obtenerPorId() - Debería devolver Optional vacío si no existe")
        void obtenerPorId_DeberiaRetornarVacioSiNoExiste() {
            // Arrange
            Long id = 999L;
            when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

            // Act
            Optional<Categoria> resultado = categoriaService.obtenerPorId(id);

            // Assert
            assertThat(resultado).isEmpty();
            verify(categoriaRepository).findById(id);
        }

        @Test
        @DisplayName("obtenerPorId() - Debería lanzar excepción con ID nulo")
        void obtenerPorId_DeberiaLanzarExcepcionConIdNulo() {
            // Act & Assert
            assertThatThrownBy(() -> categoriaService.obtenerPorId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El ID no puede ser nulo");

            verify(categoriaRepository, never()).findById(any());
        }

        @Test
        @DisplayName("obtenerPorId() - Debería lanzar excepción con ID menor o igual a 0")
        void obtenerPorId_DeberiaLanzarExcepcionConIdInvalido() {
            // Act & Assert
            assertThatThrownBy(() -> categoriaService.obtenerPorId(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El ID debe ser mayor a 0");

            assertThatThrownBy(() -> categoriaService.obtenerPorId(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El ID debe ser mayor a 0");

            verify(categoriaRepository, never()).findById(any());
        }

        @Test
        @DisplayName("obtenerPorNombre() - Debería encontrar categoría por nombre exacto")
        void obtenerPorNombre_DeberiaEncontrarPorNombreExacto() {
            // Arrange
            String nombre = "Electrónicos";
            when(categoriaRepository.findByNombreIgnoreCase(nombre))
                    .thenReturn(Optional.of(categoriaElectronicos));

            // Act
            Optional<Categoria> resultado = categoriaService.obtenerPorNombre(nombre);

            // Assert
            assertThat(resultado)
                    .isPresent()
                    .contains(categoriaElectronicos);
            verify(categoriaRepository).findByNombreIgnoreCase(nombre);
        }

        @Test
        @DisplayName("obtenerPorNombre() - Debería encontrar categoría ignorando case")
        void obtenerPorNombre_DeberiaIgnorarCase() {
            // Arrange
            String nombre = "ELECTRÓNICOS";
            when(categoriaRepository.findByNombreIgnoreCase(nombre))
                    .thenReturn(Optional.of(categoriaElectronicos));

            // Act
            Optional<Categoria> resultado = categoriaService.obtenerPorNombre(nombre);

            // Assert
            assertThat(resultado)
                    .isPresent()
                    .contains(categoriaElectronicos);
            verify(categoriaRepository).findByNombreIgnoreCase(nombre);
        }

        @Test
        @DisplayName("obtenerPorNombre() - Debería normalizar espacios")
        void obtenerPorNombre_DeberiaNormalizarEspacios() {
            // Arrange
            String nombreConEspacios = "  Electrónicos  ";
            String nombreNormalizado = "Electrónicos";
            when(categoriaRepository.findByNombreIgnoreCase(nombreNormalizado))
                    .thenReturn(Optional.of(categoriaElectronicos));

            // Act
            Optional<Categoria> resultado = categoriaService.obtenerPorNombre(nombreConEspacios);

            // Assert
            assertThat(resultado)
                    .isPresent()
                    .contains(categoriaElectronicos);
            verify(categoriaRepository).findByNombreIgnoreCase(nombreNormalizado);
        }

        @Test
        @DisplayName("obtenerPorNombre() - Debería lanzar excepción con nombre nulo")
        void obtenerPorNombre_DeberiaLanzarExcepcionConNombreNulo() {
            // Act & Assert
            assertThatThrownBy(() -> categoriaService.obtenerPorNombre(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre de la categoría no puede estar vacío");

            verify(categoriaRepository, never()).findByNombreIgnoreCase(any());
        }

        @Test
        @DisplayName("obtenerPorNombre() - Debería lanzar excepción con nombre vacío")
        void obtenerPorNombre_DeberiaLanzarExcepcionConNombreVacio() {
            // Act & Assert
            assertThatThrownBy(() -> categoriaService.obtenerPorNombre(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre de la categoría no puede estar vacío");

            assertThatThrownBy(() -> categoriaService.obtenerPorNombre("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre de la categoría no puede estar vacío");

            verify(categoriaRepository, never()).findByNombreIgnoreCase(any());
        }

        @Test
        @DisplayName("buscarPorNombre() - Debería buscar categorías que contengan texto")
        void buscarPorNombre_DeberiaBuscarQueCotienganTexto() {
            // Arrange
            String busqueda = "libr";
            List<Categoria> resultadoEsperado = Arrays.asList(categoriaLibros);
            when(categoriaRepository.findByNombreContainingIgnoreCase(busqueda))
                    .thenReturn(resultadoEsperado);

            // Act
            List<Categoria> resultado = categoriaService.buscarPorNombre(busqueda);

            // Assert
            assertThat(resultado)
                    .hasSize(1)
                    .containsExactly(categoriaLibros);
            verify(categoriaRepository).findByNombreContainingIgnoreCase(busqueda);
        }

        @Test
        @DisplayName("obtenerRequerida() - Debería devolver categoría si existe")
        void obtenerRequerida_DeberiaRetornarCategoriaExistente() {
            // Arrange
            Long id = 1L;
            when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoriaElectronicos));

            // Act
            Categoria resultado = categoriaService.obtenerRequerida(id);

            // Assert
            assertThat(resultado).isEqualTo(categoriaElectronicos);
            verify(categoriaRepository).findById(id);
        }

        @Test
        @DisplayName("obtenerRequerida() - Debería lanzar excepción si no existe")
        void obtenerRequerida_DeberiaLanzarExcepcionSiNoExiste() {
            // Arrange
            Long id = 999L;
            when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.obtenerRequerida(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Categoría no encontrada con ID: " + id);

            verify(categoriaRepository).findById(id);
        }
    }

    // ==================== OPERACIONES CRUD ====================

    @Nested
    @DisplayName("Operaciones CRUD")
    class OperacionesCrud {

        @Test
        @DisplayName("crear() - Debería crear categoría exitosamente")
        void crear_DeberiaCrearCategoriaExitosamente() {
            // Arrange
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("Nueva Categoría")
                    .descripcion("Descripción de prueba")
                    .build();

            Categoria categoriaGuardada = Categoria.builder()
                    .id(4L)
                    .nombre("Nueva Categoría")
                    .descripcion("Descripción de prueba")
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            when(categoriaRepository.existsByNombre(nuevaCategoria.getNombre()))
                    .thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenReturn(categoriaGuardada);

            // Act
            Categoria resultado = categoriaService.crear(nuevaCategoria);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(4L);
            assertThat(resultado.getNombre()).isEqualTo("Nueva Categoría");
            assertThat(resultado.getDescripcion()).isEqualTo("Descripción de prueba");

            verify(categoriaRepository).existsByNombre("Nueva Categoría");
            verify(categoriaRepository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("crear() - Debería normalizar nombre y descripción")
        void crear_DeberiaNormalizarNombreYDescripcion() {
            // Arrange
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("  Nueva Categoría  ")
                    .descripcion("  Descripción con espacios  ")
                    .build();

            when(categoriaRepository.existsByNombre("Nueva Categoría"))
                    .thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenAnswer(invocation -> {
                        Categoria categoria = invocation.getArgument(0);
                        categoria.setId(4L);
                        return categoria;
                    });

            // Act
            Categoria resultado = categoriaService.crear(nuevaCategoria);

            // Assert
            assertThat(resultado.getNombre()).isEqualTo("Nueva Categoría");
            assertThat(resultado.getDescripcion()).isEqualTo("Descripción con espacios");
        }

//        @Test
//        @DisplayName("crear() - Debería lanzar excepción con categoría nula")
//        void crear_DeberiaLanzarExcepcionConCategoriaNula() {
//            // Act & Assert
//            assertThatThrownBy(() -> categoriaService.crear(null))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessage("La categoría no puede ser nula");
//
//            verify(categoriaRepository, never()).save(any());
//        }

        @Test
        @DisplayName("crear() - Debería lanzar excepción con nombre duplicado")
        void crear_DeberiaLanzarExcepcionConNombreDuplicado() {
            // Arrange
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("Electrónicos")
                    .descripcion("Duplicado")
                    .build();

            when(categoriaRepository.existsByNombre("Electrónicos"))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.crear(nuevaCategoria))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Ya existe una categoría con el nombre: Electrónicos");

            verify(categoriaRepository).existsByNombre("Electrónicos");
            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("crear() - Debería lanzar excepción con nombre muy largo")
        void crear_DeberiaLanzarExcepcionConNombreMuyLargo() {
            // Arrange
            String nombreLargo = "A".repeat(101); // 101 caracteres
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre(nombreLargo)
                    .descripcion("Test")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.crear(nuevaCategoria))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre de la categoría no puede exceder 100 caracteres");

            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("crear() - Debería lanzar excepción con nombre muy corto")
        void crear_DeberiaLanzarExcepcionConNombreMuyCorto() {
            // Arrange
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("A") // Solo 1 caracter
                    .descripcion("Test")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.crear(nuevaCategoria))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El nombre de la categoría debe tener al menos 2 caracteres");

            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("actualizar() - Debería actualizar categoría exitosamente")
        void actualizar_DeberiaActualizarExitosamente() {
            // Arrange
            Long id = 1L;
            Categoria categoriaActualizada = Categoria.builder()
                    .nombre("Electrónicos Actualizados")
                    .descripcion("Nueva descripción")
                    .build();

            when(categoriaRepository.findById(id))
                    .thenReturn(Optional.of(categoriaElectronicos));
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Categoria resultado = categoriaService.actualizar(id, categoriaActualizada);

            // Assert
            assertThat(resultado.getNombre()).isEqualTo("Electrónicos Actualizados");
            assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción");

            verify(categoriaRepository).findById(id);
            verify(categoriaRepository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("actualizar() - Debería validar nombre único solo si cambió")
        void actualizar_DeberiaValidarNombreUnicoSoloCambio() {
            // Arrange
            Long id = 1L;
            Categoria categoriaActualizada = Categoria.builder()
                    .nombre("Electrónicos") // Mismo nombre
                    .descripcion("Nueva descripción")
                    .build();

            when(categoriaRepository.findById(id))
                    .thenReturn(Optional.of(categoriaElectronicos));
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Categoria resultado = categoriaService.actualizar(id, categoriaActualizada);

            // Assert
            assertThat(resultado).isNotNull();
            verify(categoriaRepository).findById(id);
            verify(categoriaRepository).save(any(Categoria.class));
            // No debe verificar existencia porque el nombre no cambió
            verify(categoriaRepository, never()).existsByNombre(anyString());
        }

        @Test
        @DisplayName("actualizar() - Debería lanzar excepción si no existe categoría")
        void actualizar_DeberiaLanzarExcepcionSiNoExiste() {
            // Arrange
            Long id = 999L;
            Categoria categoriaActualizada = Categoria.builder()
                    .nombre("Nueva")
                    .build();

            when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.actualizar(id, categoriaActualizada))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Categoría no encontrada con ID: " + id);

            verify(categoriaRepository).findById(id);
            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("eliminar() - Debería eliminar categoría sin productos")
        void eliminar_DeberiaEliminarSinProductos() {
            // Arrange
            Long id = 1L;
            when(categoriaRepository.findById(id))
                    .thenReturn(Optional.of(categoriaElectronicos));
            when(categoriaRepository.hasProductosAsociados(id))
                    .thenReturn(false);

            // Act
            assertThatCode(() -> categoriaService.eliminar(id))
                    .doesNotThrowAnyException();

            // Assert
            verify(categoriaRepository).findById(id);
            verify(categoriaRepository).hasProductosAsociados(id);
            verify(categoriaRepository).deleteById(id);
        }

        @Test
        @DisplayName("eliminar() - Debería lanzar excepción con productos asociados")
        void eliminar_DeberiaLanzarExcepcionConProductosAsociados() {
            // Arrange
            Long id = 1L;
            when(categoriaRepository.findById(id))
                    .thenReturn(Optional.of(categoriaElectronicos));
            when(categoriaRepository.hasProductosAsociados(id))
                    .thenReturn(true);
            when(categoriaRepository.countProductosByCategoriaId(id))
                    .thenReturn(5L);

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.eliminar(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No se puede eliminar la categoría 'Electrónicos' porque tiene 5 producto(s) asociado(s)");

            verify(categoriaRepository).findById(id);
            verify(categoriaRepository).hasProductosAsociados(id);
            verify(categoriaRepository).countProductosByCategoriaId(id);
            verify(categoriaRepository, never()).deleteById(any());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("existe() - Debería retornar true si categoría existe")
        void existe_DeberiaRetornarTrueSiExiste() {
            // Arrange
            String nombre = "Electrónicos";
            when(categoriaRepository.existsByNombre(nombre))
                    .thenReturn(true);

            // Act
            boolean resultado = categoriaService.existe(nombre);

            // Assert
            assertThat(resultado).isTrue();
            verify(categoriaRepository).existsByNombre(nombre);
        }

        @Test
        @DisplayName("existe() - Debería retornar false si categoría no existe")
        void existe_DeberiaRetornarFalseSiNoExiste() {
            // Arrange
            String nombre = "Inexistente";
            when(categoriaRepository.existsByNombre(nombre))
                    .thenReturn(false);

            // Act
            boolean resultado = categoriaService.existe(nombre);

            // Assert
            assertThat(resultado).isFalse();
            verify(categoriaRepository).existsByNombre(nombre);
        }

        @Test
        @DisplayName("contarProductos() - Debería retornar cantidad correcta")
        void contarProductos_DeberiaRetornarCantidadCorrecta() {
            // Arrange
            Long categoriaId = 1L;
            Long cantidadEsperada = 10L;
            when(categoriaRepository.countProductosByCategoriaId(categoriaId))
                    .thenReturn(cantidadEsperada);

            // Act
            Long resultado = categoriaService.contarProductos(categoriaId);

            // Assert
            assertThat(resultado).isEqualTo(cantidadEsperada);
            verify(categoriaRepository).countProductosByCategoriaId(categoriaId);
        }

        @Test
        @DisplayName("tieneProductosAsociados() - Debería retornar estado correcto")
        void tieneProductosAsociados_DeberiaRetornarEstadoCorrecto() {
            // Arrange
            Long categoriaId = 1L;
            when(categoriaRepository.hasProductosAsociados(categoriaId))
                    .thenReturn(true);

            // Act
            boolean resultado = categoriaService.tieneProductosAsociados(categoriaId);

            // Assert
            assertThat(resultado).isTrue();
            verify(categoriaRepository).hasProductosAsociados(categoriaId);
        }

        @Test
        @DisplayName("obtenerCategoriasConProductos() - Debería retornar categorías con productos")
        void obtenerCategoriasConProductos_DeberiaRetornarConProductos() {
            // Arrange
            List<Categoria> categoriasConProductos = Arrays.asList(categoriaElectronicos, categoriaRopa);
            when(categoriaRepository.findCategoriasConProductos())
                    .thenReturn(categoriasConProductos);

            // Act
            List<Categoria> resultado = categoriaService.obtenerCategoriasConProductos();

            // Assert
            assertThat(resultado)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(categoriaElectronicos, categoriaRopa);
            verify(categoriaRepository).findCategoriasConProductos();
        }

        @Test
        @DisplayName("obtenerCategoriasSinProductos() - Debería retornar categorías sin productos")
        void obtenerCategoriasSinProductos_DeberiaRetornarSinProductos() {
            // Arrange
            List<Categoria> categoriasSinProductos = Arrays.asList(categoriaLibros);
            when(categoriaRepository.findCategoriasSinProductos())
                    .thenReturn(categoriasSinProductos);

            // Act
            List<Categoria> resultado = categoriaService.obtenerCategoriasSinProductos();

            // Assert
            assertThat(resultado)
                    .hasSize(1)
                    .containsExactly(categoriaLibros);
            verify(categoriaRepository).findCategoriasSinProductos();
        }
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    @Nested
    @DisplayName("Casos Edge y Validaciones")
    class CasosEdgeYValidaciones {

        @Test
        @DisplayName("Debería manejar descripción nula en creación")
        void deberiaManejarDescripcionNulaEnCreacion() {
            // Arrange
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("Sin Descripción")
                    .descripcion(null)
                    .build();

            when(categoriaRepository.existsByNombre("Sin Descripción"))
                    .thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenAnswer(invocation -> {
                        Categoria categoria = invocation.getArgument(0);
                        categoria.setId(4L);
                        return categoria;
                    });

            // Act
            Categoria resultado = categoriaService.crear(nuevaCategoria);

            // Assert
            assertThat(resultado.getDescripcion()).isNull();
            verify(categoriaRepository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción con descripción muy larga")
        void deberiaLanzarExcepcionConDescripcionMuyLarga() {
            // Arrange
            String descripcionLarga = "A".repeat(256); // 256 caracteres
            Categoria nuevaCategoria = Categoria.builder()
                    .nombre("Test")
                    .descripcion(descripcionLarga)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.crear(nuevaCategoria))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La descripción no puede exceder 255 caracteres");

            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería manejar eliminación con reubicación - IDs iguales")
        void deberiaManejarEliminacionConReubicacionIdsIguales() {
            // Arrange
            Long id = 1L;

            // Act & Assert
            assertThatThrownBy(() -> categoriaService.eliminarConReubicacion(id, id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La categoría de destino no puede ser la misma que se va a eliminar");

            verify(categoriaRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Debería manejar eliminación con reubicación - categoría destino no existe")
        void deberiaManejarEliminacionConReubicacionDestinoNoExiste() {
            // Arrange
            Long categoriaEliminarId = 1L;
            Long categoriaNuevaId = 999L;
            when(categoriaRepository.findById(categoriaEliminarId))
                    .thenReturn(Optional.of(categoriaElectronicos));
            when(categoriaRepository.findById(categoriaNuevaId))
                    .thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> categoriaService.eliminarConReubicacion(categoriaEliminarId, categoriaNuevaId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Categoría de destino no encontrada con ID: " + categoriaNuevaId);
            verify(categoriaRepository).findById(categoriaEliminarId);
            verify(categoriaRepository).findById(categoriaNuevaId);
        }
    }
}
