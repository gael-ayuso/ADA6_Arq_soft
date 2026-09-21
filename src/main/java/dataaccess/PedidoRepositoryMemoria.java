package dataaccess;

import business.Pedido;
import data.BaseDatosMemoria;

import java.util.Map;

public class PedidoRepositoryMemoria implements PedidoRepository {
    private final Map<Integer, Pedido> baseDatosMemoria;

    public PedidoRepositoryMemoria(Map<Integer, Pedido> baseDatosMemoria) {
        this.baseDatosMemoria = baseDatosMemoria;
    }

    @Override
    public int guardar(Pedido pedido) {
        baseDatosMemoria.put(pedido.getId(), pedido);
        return pedido.getId();
    }

    @Override
    public Pedido buscarPorId(int id) {
        return this.baseDatosMemoria.get(id);
    }
}
