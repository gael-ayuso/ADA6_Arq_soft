package business;

import business.models.Pedido;
import business.services.Tuberia;
import business.services.filtros.*;
import dataaccess.PedidoRepository;

import java.util.List;

public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final Tuberia tuberia;

    public PedidoService(PedidoRepository pedidoRepository) {
        this(pedidoRepository, new Tuberia(List.of(
                new ValidarDatos(),
                new ComprobarDisponibilidad(),
                new CalcularSubtotal(),
                new VerificarFraude(),
                new AplicarDescuento(),
                new CalcularImpuestos(),
                new CalcularTotal(),
                new ConfirmarPedido()
        )));
    }

    public PedidoService(PedidoRepository pedidoRepository, Tuberia tuberia) {
        this.pedidoRepository = pedidoRepository;
        this.tuberia = tuberia;
    }

    public Pedido procesarPedido(Pedido pedido) {
        return this.tuberia.procesarPedido(pedido);
    }

    public Pedido procesarYGuardar(Pedido pedido) {
        Pedido procesado = this.tuberia.procesarPedido(pedido);
        if (procesado != null) {
            this.pedidoRepository.guardar(procesado);
        }
        return procesado;
    }

    /**
     * Registra un pedido aplicando las reglas de negocio y persistiendo el resultado.
     * Nombre de método según la especificación del requerimiento ADA6.
     */
    public Pedido registrar(Pedido pedido) {
        return procesarYGuardar(pedido);
    }

    public Pedido buscarPorId(int id) {
        return this.pedidoRepository.buscarPorId(id);
    }

    public List<Pedido> listarPedidos() {
        return this.pedidoRepository.listarTodos();
    }

    public List<business.models.Producto> obtenerCatalogoProductos() {
        return dataaccess.CatalogoProductosArchivo.cargarCatalogo();
    }
}
