package business;

import java.util.List;

public class Pedido {
    private final int id;
    private final String cliente;
    private final List<Producto> productos;
    private final double subtotal;
    private final double descuento;
    private final double impuestos;
    private final double total;
    private final EstadoPedido estado;
    private Pedido(int id, String cliente, List<Producto> productos, double subtotal, double descuento, double impuestos, double total, EstadoPedido estado) {
        this.id = id;
        this.cliente = cliente;
        this.productos = productos;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.impuestos = impuestos;
        this.total = total;
        this.estado = estado;
    }

    public Pedido(int id, String cliente, List<Producto> productos) {
        this(id, cliente, productos, 0, 0, 0, 0, EstadoPedido.PEDIDO_PENDIENTE);
    }

    public int getId() {
        return id;
    }

}
