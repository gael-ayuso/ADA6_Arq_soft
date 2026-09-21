package business.services.filtros;


import business.models.EstadoPedido;
import business.models.Pedido;

public class ValidarDatos implements Filtro{
    @Override
    public Pedido procesar(Pedido pedido) {
        final boolean isClienteValido = pedido.getCliente() != null && !pedido.getCliente().isEmpty();
        final boolean isListaProductosValida = pedido.getListaProductos() != null && !pedido.getListaProductos().isEmpty();

        if (!isClienteValido) {
            throw new RuntimeException("El cliente no puede estar vacio");
        }else if (!isListaProductosValida) {
            pedido.setEstado(EstadoPedido.PEDIDO_SIN_PRODUCTOS);
            throw new RuntimeException("El pedido no puede estar vacio");
        }else {
            pedido.setEstado(EstadoPedido.PEDIDO_VALIDO);
        }
        return pedido;
    }
}
