package business.services.filtros;


import business.models.EstadoPedido;
import business.models.Pedido;
import business.models.Producto;

public class ValidarDatos implements Filtro{
    @Override
    public Pedido procesar(Pedido pedido) {
        if (pedido == null) {
            throw new RuntimeException("El pedido no puede ser nulo");
        }

        final boolean isClienteValido = pedido.getCliente() != null && !pedido.getCliente().trim().isEmpty();
        final boolean isListaProductosValida = pedido.getListaProductos() != null && !pedido.getListaProductos().isEmpty();

        if (!isClienteValido) {
            throw new RuntimeException("El cliente no puede estar vacio");
        } else if (!isListaProductosValida) {
            pedido.setEstado(EstadoPedido.PEDIDO_SIN_PRODUCTOS);
            throw new RuntimeException("El pedido no puede estar vacio");
        }

        // Regla de negocio ADA6: La cantidad solicitada debe ser mayor que cero
        for (Producto producto : pedido.getListaProductos()) {
            if (producto == null || producto.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad solicitada debe ser mayor que cero para cada producto");
            }
        }

        pedido.setEstado(EstadoPedido.PEDIDO_VALIDO);
        return pedido;
    }
}
