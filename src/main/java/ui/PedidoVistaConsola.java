package ui;

import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Implementacion de la capa de presentacion mediante interfaz de consola.
 * Cumple con la separacion de responsabilidades:
 * - Captura datos y solicitudes del usuario.
 * - Invoca a la capa de negocio (PedidoService).
 * - Muestra resultados y errores sin calcular totales ni acceder a la persistencia directamente.
 * - No contiene emojis para mantener una presentacion formal y profesional.
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
            System.out.println("      SISTEMA DE PEDIDOS - CAPA DE PRESENTACION");
            System.out.println("========================================================");
            System.out.println("1. Registrar pedido");
            System.out.println("2. Consultar pedido por ID");
            System.out.println("3. Listar pedidos");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opcion: ");

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
                    System.out.println("[INFO] Saliendo de la aplicacion...");
                    salir = true;
                    break;
                default:
                    System.out.println("[ERROR] Opcion no valida. Intente de nuevo.");
            }
        }
    }

    private void menuRegistrarPedido() {
        System.out.println("\n--- REGISTRAR NUEVO PEDIDO ---");
        System.out.print("Nombre del cliente: ");
        String cliente = scanner.nextLine().trim();

        List<Producto> catalogo = pedidoService.obtenerCatalogoProductos();
        List<Producto> productosSeleccionados = new ArrayList<>();

        boolean agregando = true;
        while (agregando) {
            System.out.println("\nCatalogo de productos disponibles:");
            for (int i = 0; i < catalogo.size(); i++) {
                Producto prod = catalogo.get(i);
                System.out.printf("  %d) %s - $%.2f (Existencia: %d)%n",
                        (i + 1), prod.getNombre(), prod.getPrecio(), prod.getExistencia());
            }
            int opcPersonalizado = catalogo.size() + 1;
            int opcTerminar = catalogo.size() + 2;

            System.out.printf("  %d) [Ingresar producto personalizado manual]%n", opcPersonalizado);
            System.out.printf("  %d) [Terminar y procesar pedido]%n", opcTerminar);
            System.out.print("Seleccione una opcion: ");

            String input = scanner.nextLine().trim();
            try {
                int seleccion = Integer.parseInt(input);
                if (seleccion == opcTerminar) {
                    agregando = false;
                } else if (seleccion == opcPersonalizado) {
                    agregarProductoManual(productosSeleccionados);
                } else if (seleccion >= 1 && seleccion <= catalogo.size()) {
                    Producto seleccionado = catalogo.get(seleccion - 1);
                    System.out.print("Cantidad a solicitar de '" + seleccionado.getNombre() + "': ");
                    int cantidad = Integer.parseInt(scanner.nextLine().trim());

                    productosSeleccionados.add(new Producto(
                            seleccionado.getNombre(),
                            seleccionado.getPrecio(),
                            cantidad,
                            seleccionado.getExistencia()
                    ));
                    System.out.println("[OK] Producto agregado a la orden.");
                } else {
                    System.out.println("[ERROR] Opcion fuera de rango.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Debe ingresar un numero entero valido.");
            }
        }

        if (productosSeleccionados.isEmpty()) {
            System.out.println("[AVISO] No se seleccionaron productos. Operacion cancelada.");
            return;
        }

        Pedido nuevoPedido = new Pedido(cliente, productosSeleccionados);
        procesarNuevoPedido(nuevoPedido);
    }

    private void agregarProductoManual(List<Producto> lista) {
        try {
            System.out.print("Nombre del producto: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Precio unitario: ");
            double precio = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Cantidad solicitada: ");
            int cantidad = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Existencia en inventario: ");
            int existencia = Integer.parseInt(scanner.nextLine().trim());

            lista.add(new Producto(nombre, precio, cantidad, existencia));
            System.out.println("[OK] Producto personalizado agregado a la orden.");
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Datos numericos invalidos. Producto no agregado.");
        }
    }

    private void menuConsultarPedido() {
        System.out.print("\nIngrese el ID del pedido a consultar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            consultarPedidoPorId(id);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] ID invalido. Debe ser un numero entero.");
        }
    }

    public void mostrarListadoPedidos() {
        System.out.println("\n========================================================");
        System.out.println("[CAPA PRESENTACION] Listando todos los pedidos");
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
        System.out.println("[CAPA PRESENTACION] Enviando pedido a procesamiento");
        System.out.println("Cliente: " + pedido.getCliente());
        System.out.println("Cantidad de productos: " + (pedido.getListaProductos() != null ? pedido.getListaProductos().size() : 0));
        System.out.println("========================================================");

        try {
            Pedido pedidoProcesado = pedidoService.procesarYGuardar(pedido);
            mostrarResumenPedido(pedidoProcesado);
            return pedidoProcesado;
        } catch (Exception e) {
            System.out.println("[ERROR] Error en el procesamiento del pedido: " + e.getMessage());
            return null;
        }
    }

    public void consultarPedidoPorId(int id) {
        System.out.println("\n[CAPA PRESENTACION] Consultando pedido con ID: " + id);
        Pedido pedido = pedidoService.buscarPorId(id);
        if (pedido != null) {
            System.out.println("[OK] Pedido encontrado:");
            mostrarResumenPedido(pedido);
        } else {
            System.out.println("[ERROR] No se encontro ningun pedido con ID: " + id);
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
            System.out.println("[ALERTA] Marcado por revision de fraude: SI");
        }
        System.out.println("-----------------------------------------------------");
    }
}
