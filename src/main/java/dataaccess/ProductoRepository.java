package dataaccess;

import business.models.Producto;

import java.util.List;

/**
 * Contrato para el acceso a datos del catalogo de productos.
 * Permite desacoplar el origen de datos (CSV, TXT, JSON, Base de Datos) de la logica de negocio.
 */
public interface ProductoRepository {
    List<Producto> obtenerTodos();
}

