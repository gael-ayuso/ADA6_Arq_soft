package ui;

import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Implementación de la capa de presentación mediante interfaz de consola.
 * Cumple con la separación de responsabilidades:
 * - Captura datos y solicitudes del usuario.
 * - Invoca a la capa de negocio (PedidoService).
 * - Muestra resultados y errores sin calcular totales ni acceder a la persistencia directamente.
 */
public class PedidoVistaConsola implements PedidoVista {
    private final PedidoService pedidoService;
    private final Scanner scanner;

    public PedidoVistaConsola(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n========================================================");
            System.out.println("      SISTEMA DE PEDIDOS - CAPA DE PRESENTACIÓN");
            System.out.println("========================================================");
            System.out.println("1. Registrar pedido");
            System.out.println("2. Consultar pedido por ID");
            System.out.println("3. Listar pedidos");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1":
                    menuRegistrarPedido();
                    break;
                case "2":
                    menuConsultarPedido();
                    break;
                case "3":
                    mostrarListadoPedidos();
                    break;
                case "4":
                    System.out.println("Saliendo de la aplicación...");
                    salir = true;
                    break;
                default:
                    System.out.println("❌ Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void menuRegistrarPedido() {
        System.out.println("\n--- REGISTRAR NUEVO PEDIDO ---");
        System.out.print("Nombre del cliente: ");
        String cliente = scanner.nextLine().trim();

        List<Producto> catalogo = obtenerCatalogoPredefinido();
        List<Producto> productosSeleccionados = new ArrayList<>();

        boolean agregando = true;
        while (agregando) {
            System.out.println("\nCatálogo de productos disponibles:");
            for (int i = 0; i < catalogo.size(); i++) {
                Producto prod = catalogo.get(i);
                System.out.printf("  %d) %s - $%.2f (Stock: %d)%n",
                        (i + 1), prod.getNombre(), prod.getPrecio(), prod.getExistencia());
            }
            System.out.printf("  %d) [Terminar y procesar pedido]%n", catalogo.size() + 1);
            System.out.print("Seleccione un producto: ");

            String input = scanner.nextLine().trim();
            try {
                int opcProd = Integer.parseInt(input);
                if (opcProd == catalogo.size() + 1) {
                    agregando = false;
                } else if (opcProd >= 1 && opcProd <= catalogo.size()) {
                    Producto seleccionado = catalogo.get(opcProd - 1);
                    System.out.print("Cantidad a solicitar de '" + seleccionado.getNombre() + "': ");
                    int cantidad = Integer.parseInt(scanner.nextLine().trim());

                    productosSeleccionados.add(new Producto(
                            seleccionado.getNombre(),
                            seleccionado.getPrecio(),
                            cantidad,
                            seleccionado.getExistencia()
                    ));
                    System.out.println("✔ Producto agregado a la orden.");
                } else {
                    System.out.println("❌ Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor ingrese un número válido.");
            }
        }

        if (productosSeleccionados.isEmpty()) {
            System.out.println("⚠️ No se seleccionaron productos. Operación cancelada.");
            return;
        }

        Pedido nuevoPedido = new Pedido(cliente, productosSeleccionados);
        procesarNuevoPedido(nuevoPedido);
    }

    private void menuConsultarPedido() {
        System.out.print("\nIngrese el ID del pedido a consultar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            consultarPedidoPorId(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID inválido. Debe ser un número entero.");
        }
    }

    public void mostrarListadoPedidos() {
        System.out.println("\n========================================================");
        System.out.println(">>> [CAPA PRESENTACIÓN] Listando todos los pedidos");
        System.out.println("========================================================");

        List<Pedido> pedidos = pedidoService.listarPedidos();
        if (pedidos == null || pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados en el sistema.");
            return;
        }

        System.out.printf("%-5s | %-18s | %-10s | %-10s | %-10s | %-12s%n",
                "ID", "CLIENTE", "SUBTOTAL", "DESCUENTO", "TOTAL", "ESTADO");
        System.out.println("-------------------------------------------------------------------------");
        for (Pedido p : pedidos) {
            System.out.printf("%-5d | %-18s | $%-9.2f | $%-9.2f | $%-9.2f | %-12s%n",
                    p.getId(),
                    (p.getCliente().length() > 18 ? p.getCliente().substring(0, 15) + "..." : p.getCliente()),
                    p.getSubtotal(),
                    p.getDescuento(),
                    p.getTotal(),
                    p.getEstado());
        }
        System.out.println("-------------------------------------------------------------------------");
    }

    public Pedido procesarNuevoPedido(Pedido pedido) {
        System.out.println("\n========================================================");
        System.out.println(">>> [CAPA PRESENTACIÓN] Enviando pedido a procesamiento");
        System.out.println("Cliente: " + pedido.getCliente());
        System.out.println("Cantidad de productos: " + (pedido.getListaProductos() != null ? pedido.getListaProductos().size() : 0));
        System.out.println("========================================================");

        try {
            Pedido pedidoProcesado = pedidoService.procesarYGuardar(pedido);
            mostrarResumenPedido(pedidoProcesado);
            return pedidoProcesado;
        } catch (Exception e) {
            System.out.println("❌ Error en el procesamiento del pedido: " + e.getMessage());
            return null;
        }
    }

    public void consultarPedidoPorId(int id) {
        System.out.println("\n>>> [CAPA PRESENTACIÓN] Consultando pedido con ID: " + id);
        Pedido pedido = pedidoService.buscarPorId(id);
        if (pedido != null) {
            System.out.println("✔ Pedido encontrado:");
            mostrarResumenPedido(pedido);
        } else {
            System.out.println("❌ No se encontró ningún pedido con ID: " + id);
        }
    }

    private void mostrarResumenPedido(Pedido pedido) {
        System.out.println("---------------- Resumen del Pedido -----------------");
        System.out.println("ID: " + pedido.getId());
        System.out.println("Cliente: " + pedido.getCliente());
        System.out.println("Estado: " + pedido.getEstado());
        System.out.printf("Subtotal: $%.2f%n", pedido.getSubtotal());
        System.out.printf("Descuento: $%.2f%n", pedido.getDescuento());
        System.out.printf("Impuestos (IVA): $%.2f%n", pedido.getImpuestos());
        System.out.printf("Total a pagar: $%.2f%n", pedido.getTotal());
        if (pedido.isRevisionFraude()) {
            System.out.println("⚠️ Marcado por revisión de fraude: SÍ");
        }
        System.out.println("-----------------------------------------------------");
    }

    public static List<Producto> obtenerCatalogoPredefinido() {
        return List.of(
                new Producto("Laptop Gamer", 1500.0, 0, 5),
                new Producto("Mouse Inalámbrico", 250.0, 0, 10),
                new Producto("Teclado Mecánico", 350.0, 0, 8),
                new Producto("Monitor 4K", 800.0, 0, 3),
                new Producto("Servidor Enterprise", 6000.0, 0, 2),
                new Producto("Memoria USB 64GB", 150.0, 0, 20)
        );
    }
}
