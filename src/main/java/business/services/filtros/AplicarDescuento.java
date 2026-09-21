package business.services.filtros;


import business.models.EstadoPedido;
import business.models.Pedido;

public class AplicarDescuento implements Filtro{
    //estos 2 parametros son una base pues no se especifica exactamente
    //cual es el descuento ni la cantidad exacta para obtenerlo
    private static final double UMBRAL_DESCUENTO = 1000.0;
    private static final double PORCENTAJE_DESCUENTO= 0.1;

    @Override
    public Pedido procesar(Pedido pedido) {
        if (pedido == null ){
            return null;
        }

        if (pedido.getSubtotal() >= UMBRAL_DESCUENTO) {
            double descuento = pedido.getSubtotal() * PORCENTAJE_DESCUENTO;

            pedido.setDescuento(descuento);
            pedido.setEstado(EstadoPedido.PEDIDO_CON_DESCUENTO);
        } else {
            pedido.setDescuento(0);
            pedido.setEstado(EstadoPedido.PEDIDO_SIN_DESCUENTO);
        }

        return pedido;
    }
}
