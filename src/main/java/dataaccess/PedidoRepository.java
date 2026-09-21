package dataaccess;

import business.models.Pedido;

public interface PedidoRepository {
    int guardar(Pedido pedido);

    Pedido buscarPorId(int id);
}
