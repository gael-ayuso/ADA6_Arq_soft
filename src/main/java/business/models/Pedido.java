package business.models;

import java.util.List;

public class Pedido {
    private int id;
    private final String cliente;
    private final List<Producto> listaProductos;
    //Los volvi doubles para calculos más exactos
    private double subtotal;
    private double descuento;
    private double impuestos;
    private double total;
    private EstadoPedido estado;
    private boolean revisionFraude;

    public Pedido(
            String cliente,
            List<Producto> listaProductos,
            double subtotal,
            double descuento,
            double impuestos,
            double total,
            EstadoPedido estado) {
        this.cliente = cliente;
        this.listaProductos = listaProductos;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.impuestos = impuestos;
        this.total = total;
        this.estado = estado;
    }

    public Pedido(
            String cliente,
            List<Producto> listaProductos
    ) {
        this(
                cliente,
                listaProductos,
                0.0,
                0.0,
                0.0,
                0.0,
                null
        );
    }


    public String getCliente() {
        return cliente;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public boolean isRevisionFraude() {
        return revisionFraude;
    }
    public void setRevisionFraude(boolean revisionFraude) {
        this.revisionFraude = revisionFraude;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente='" + cliente + '\'' +
                ", productos=" + (listaProductos != null ? listaProductos.size() : 0) +
                ", subtotal=$" + String.format("%.2f", subtotal) +
                ", revisionFraude=" + revisionFraude + //nuevo flujo
                ", descuento=$" + String.format("%.2f", descuento) +
                ", impuestos=$" + String.format("%.2f", impuestos) +
                ", total=$" + String.format("%.2f", total) +
                ", estado=" + estado +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
