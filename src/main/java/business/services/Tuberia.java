package business.services;


import business.models.Pedido;
import business.services.filtros.Filtro;

import java.util.List;

public class Tuberia {
    private final List<Filtro> pipeline;
    public Tuberia(List<Filtro> pipeline){
        this.pipeline = pipeline;
    }


    public Pedido procesarPedido(Pedido pedido){
        if (pedido == null) return null;
        for (Filtro filtro : pipeline) {
            pedido = filtro.procesar(pedido);

//            System.out.println(" Tras " + filtro.getClass().getSimpleName() +
//                               " | Estado: " + pedido.getEstado() +
//                               " | Subtotal: $" + pedido.getSubtotal());
        }
        return pedido;
    }
}
