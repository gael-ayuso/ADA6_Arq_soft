package business;

public class Producto {
    private final String nombre;
    private final double precio;
    private int cantidad;
    private int existencia;

    private Producto(String nombre, double precio, int cantidad, int existencia) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.existencia = existencia;
    }

    public Producto(String nombre, double precio) {
        this(nombre, precio, 0, 0);
    }
}
