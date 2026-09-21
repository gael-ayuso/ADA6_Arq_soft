package business.services.filtros;


import business.models.EstadoPedido;
import business.models.Pedido;

public class ConfirmarPedido implements Filtro{
    @Override
    public Pedido procesar(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        if (pedido.isRevisionFraude()) {
            return pedido;
        } else {
            pedido.setEstado(EstadoPedido.PEDIDO_CONFIRMADO);

            return pedido;
        }
        
    }
}
