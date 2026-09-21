package business.services.filtros;


import business.models.Pedido;

public interface Filtro {
    Pedido procesar(Pedido pedido);
}
