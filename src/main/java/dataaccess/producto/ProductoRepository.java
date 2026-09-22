package dataaccess.producto;

import business.models.Producto;

import java.util.List;

/**
 * Contrato para el acceso a datos del catalogo de productos.
 */
public interface ProductoRepository {
    List<Producto> obtenerTodos();
}

