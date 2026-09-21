import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;
import dataaccess.PedidoRepository;
import dataaccess.PedidoRepositoryMemoria;
import ui.PedidoVistaConsola;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   INICIALIZANDO ARQUITECTURA POR CAPAS (4 NIVELES)");
        System.out.println("==================================================================");

        Map<Integer, Pedido> baseDatosEnMemoria = new HashMap<>();
        PedidoRepository pedidoRepository = new PedidoRepositoryMemoria(baseDatosEnMemoria);
        PedidoService pedidoService = new PedidoService(pedidoRepository);
        PedidoVistaConsola vista = new PedidoVistaConsola(pedidoService);

        // ================================================================
        // CASO 1: Pedido válido con derecho a descuento (Subtotal >= $1000)
        // ================================================================
        System.out.println("\n--- PRUEBA 1: Pedido normal con descuento ---");
        List<Producto> productos1 = List.of(
                new Producto("Laptop Gamer", 1500.0, 1, 5),
                new Producto("Mouse Inalámbrico", 250.0, 2, 10)
        );
        Pedido pedido1 = new Pedido("Carlos Gómez", productos1);
        vista.procesarNuevoPedido(pedido1);

        // ================================================================
        // CASO 2: Pedido de alto valor que activa la verificación de fraude (> $5000)
        // ================================================================
        System.out.println("\n--- PRUEBA 2: Pedido con alerta de fraude ---");
        List<Producto> productos2 = List.of(
                new Producto("Servidor Enterprise", 6000.0, 1, 2)
        );
        Pedido pedido2 = new Pedido("Juan Pérez", productos2);
        vista.procesarNuevoPedido(pedido2);

        // ================================================================
        // CASO 3: Pedido con stock insuficiente (Manejo de excepción)
        // ================================================================
        System.out.println("\n--- PRUEBA 3: Pedido que excede el stock disponible ---");
        List<Producto> productos3 = List.of(
                new Producto("Monitor 4K", 800.0, 10, 3) // Pide 10, solo hay 3 en stock
        );
        Pedido pedido3 = new Pedido("Ana López", productos3);
        vista.procesarNuevoPedido(pedido3);

        // ================================================================
        // CASO 4: Consulta de persistencia a través de la capa de presentación
        // ================================================================
        System.out.println("\n--- PRUEBA 4: Consulta de pedidos persistidos ---");
        vista.consultarPedidoPorId(1);
        vista.consultarPedidoPorId(2);
        vista.consultarPedidoPorId(99); // ID inexistente
    }
}
