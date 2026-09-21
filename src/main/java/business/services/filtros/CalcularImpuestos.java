package business.services.filtros;


import business.models.Pedido;

public class CalcularImpuestos implements Filtro{
    private static final double IVA = 0.16;

    @Override
    public Pedido procesar(Pedido pedido) {
        if (pedido == null){
            return null;
        }

        double baseGravable = Math.max(0, pedido.getSubtotal() - pedido.getDescuento());
        double impuestos = baseGravable * IVA;

        pedido.setImpuestos(impuestos);

        return pedido;
    }
}
