package dataaccess;

import business.Pedido;

public interface PedidoRepository {
    int guardar(Pedido pedido);

    Pedido buscarPorId(int id);
}
