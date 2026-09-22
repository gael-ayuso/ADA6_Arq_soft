package business;

import business.models.Pedido;
import business.services.Tuberia;
import business.services.filtros.*;
import dataaccess.PedidoRepository;
import dataaccess.ProductoRepository;
import dataaccess.ProductoRepositoryArchivo;

import java.util.List;

public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final Tuberia tuberia;

    public PedidoService(PedidoRepository pedidoRepository) {
        this(pedidoRepository, new ProductoRepositoryArchivo());
    }

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this(pedidoRepository, productoRepository, new Tuberia(List.of(
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

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository, Tuberia tuberia) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.tuberia = tuberia;
    }


    public Pedido procesarYGuardar(Pedido pedido) {
        Pedido procesado = this.tuberia.procesarPedido(pedido);
        if (procesado != null) {
            this.pedidoRepository.guardar(procesado);
        }
        return procesado;
    }

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
        return this.productoRepository.obtenerTodos();
    }
}
