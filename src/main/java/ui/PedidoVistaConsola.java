package ui;

import business.PedidoService;
import business.models.Pedido;

public class PedidoVistaConsola {
    private final PedidoService pedidoService;

    public PedidoVistaConsola(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
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
        System.out.println("Marcado por revisión de fraude: " + (pedido.isRevisionFraude() ? "SÍ" : "NO"));
        System.out.println("-----------------------------------------------------");
    }
}
