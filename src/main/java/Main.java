import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;
import dataaccess.PedidoRepository;
import dataaccess.PedidoRepositoryArchivo;
import dataaccess.PedidoRepositoryMemoria;
import ui.PedidoVista;
import ui.PedidoVistaConsola;
import ui.PedidoVistaGUI;

import java.io.File;
import java.util.List;

/**
 * Punto de entrada de la aplicación.
 * Demuestra la arquitectura en capas y la inyección de dependencias:
 * - Capa de Presentación (PedidoVistaConsola / PedidoVistaGUI)
 * - Capa de Lógica de Negocio (PedidoService con Tubería y Filtros interna)
 * - Capa de Acceso a Datos (PedidoRepositoryMemoria / PedidoRepositoryArchivo)
 * - Capa de Datos / Almacenamiento (Colecciones en Memoria o Archivo de texto plano)
 */
public class Main {
    public static void main(String[] args) {
        // =========================================================================
        // 1. CAPA DE ACCESO A DATOS (Elegir implementación sin afectar al negocio)
        // =========================================================================
        // Opción A: Almacenamiento en memoria (Map)
        PedidoRepository repositorio = new PedidoRepositoryMemoria();

        // Opción B: Persistencia en archivo de texto (Segunda parte de ADA6)
        // PedidoRepository repositorio = new PedidoRepositoryArchivo("pedidos.txt");

        // =========================================================================
        // 2. CAPA DE LÓGICA DE NEGOCIO (Inyección del contrato PedidoRepository)
        // =========================================================================
        PedidoService pedidoService = new PedidoService(repositorio);

        // =========================================================================
        // 3. CAPA DE PRESENTACIÓN (Elegir GUI moderna o Consola interactiva)
        // =========================================================================
        // Si se pasa el argumento "--consola", arranca el menú en consola.
        // Si se pasa el argumento "--evidencias", ejecuta los 5 casos de prueba obligatorios.
        // Por defecto arranca la GUI moderna (Swing + FlatLaf).
        if (args != null && args.length > 0) {
            if (args[0].equalsIgnoreCase("--consola")) {
                PedidoVista vistaConsola = new PedidoVistaConsola(pedidoService);
                vistaConsola.iniciar();
                return;
            } else if (args[0].equalsIgnoreCase("--evidencias")) {
                ejecutarCincoCasosDeEvidencia();
                return;
            }
        }

        // Por defecto: interfaz gráfica moderna desacoplada
        PedidoVista vistaGUI = new PedidoVistaGUI(pedidoService);
        vistaGUI.iniciar();
    }

    /**
     * Ejecuta y demuestra los 5 casos de evidencia exigidos por la instrucción ADA6:
     * 1. Pedido válido (con descuento >= $1,000)
     * 2. Pedido sin descuento (< $1,000)
     * 3. Producto sin existencia suficiente (error de validación)
     * 4. Consulta (por ID y listado de persistencia)
     * 5. Sustitución del repositorio (Memoria -> Archivo sin alterar negocio ni UI)
     */
    public static void ejecutarCincoCasosDeEvidencia() {
        System.out.println("==================================================================");
        System.out.println("   DEMOSTRACIÓN DE LOS 5 CASOS DE EVIDENCIA (ARQUITECTURA ADA6)");
        System.out.println("==================================================================");

        // Repositorio en memoria inicial
        PedidoRepository repoMemoria = new PedidoRepositoryMemoria();
        PedidoService service = new PedidoService(repoMemoria);
        PedidoVistaConsola vista = new PedidoVistaConsola(service);

        // -----------------------------------------------------------------
        // CASO 1: Pedido válido con descuento (Subtotal >= $1,000)
        // -----------------------------------------------------------------
        System.out.println("\n>>> [CASO 1] Pedido válido con derecho a descuento (>= $1,000)");
        List<Producto> prods1 = List.of(
                new Producto("Laptop Gamer", 1500.0, 1, 5),
                new Producto("Mouse Inalámbrico", 250.0, 2, 10)
        );
        Pedido pedido1 = new Pedido("Carlos Gómez", prods1);
        vista.procesarNuevoPedido(pedido1);

        // -----------------------------------------------------------------
        // CASO 2: Pedido sin descuento (Subtotal < $1,000)
        // -----------------------------------------------------------------
        System.out.println("\n>>> [CASO 2] Pedido sin descuento (< $1,000)");
        List<Producto> prods2 = List.of(
                new Producto("Teclado Mecánico", 350.0, 1, 8),
                new Producto("Mouse Inalámbrico", 250.0, 2, 10)
        );
        Pedido pedido2 = new Pedido("Beatriz Soto", prods2);
        vista.procesarNuevoPedido(pedido2);

        // -----------------------------------------------------------------
        // CASO 3: Producto sin existencia suficiente (Manejo de regla de negocio)
        // -----------------------------------------------------------------
        System.out.println("\n>>> [CASO 3] Producto sin existencia suficiente");
        List<Producto> prods3 = List.of(
                new Producto("Monitor 4K", 800.0, 10, 3) // Pide 10, solo hay 3 en stock
        );
        Pedido pedido3 = new Pedido("Ana López", prods3);
        vista.procesarNuevoPedido(pedido3);

        // -----------------------------------------------------------------
        // CASO 4: Consulta de persistencia (por ID y listado)
        // -----------------------------------------------------------------
        System.out.println("\n>>> [CASO 4] Consulta de pedidos (por ID y Listado)");
        vista.consultarPedidoPorId(1);
        vista.consultarPedidoPorId(99); // Inexistente
        vista.mostrarListadoPedidos();

        // -----------------------------------------------------------------
        // CASO 5: Sustitución del repositorio (Memoria -> Archivo)
        // -----------------------------------------------------------------
        System.out.println("\n>>> [CASO 5] Sustitución del repositorio (Sin modificar Servicio ni Vista)");
        String archivoPrueba = "pedidos_evidencia.txt";
        new File(archivoPrueba).delete(); // Limpiar archivo previo si existe

        PedidoRepository repoArchivo = new PedidoRepositoryArchivo(archivoPrueba);
        PedidoService serviceConArchivo = new PedidoService(repoArchivo);
        PedidoVistaConsola vistaConArchivo = new PedidoVistaConsola(serviceConArchivo);

        System.out.println("Guardando pedido en persistencia de archivo:");
        Pedido pedidoArchivo = new Pedido("David Morales", List.of(
                new Producto("Monitor 4K", 800.0, 2, 3)
        ));
        vistaConArchivo.procesarNuevoPedido(pedidoArchivo);

        System.out.println("Verificando recuperación desde el archivo físico:");
        vistaConArchivo.consultarPedidoPorId(1);
        vistaConArchivo.mostrarListadoPedidos();

        // Limpiar archivo de evidencia de prueba
        new File(archivoPrueba).delete();

        System.out.println("\n==================================================================");
        System.out.println("   TODAS LAS EVIDENCIAS ARQUITECTÓNICAS EJECUTADAS CON ÉXITO");
        System.out.println("==================================================================");
    }
}
