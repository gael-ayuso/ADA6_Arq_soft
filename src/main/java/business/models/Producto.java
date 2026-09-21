package business.models;

public class Producto {
    private final String nombre;
    private final double precio;
    private int cantidad;
    private int existencia;

    public Producto(String nombre, double precio, int cantidad, int existencia) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.existencia = existencia;
    }

    public int getCantidadExistencia() {
        return existencia;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }


}
