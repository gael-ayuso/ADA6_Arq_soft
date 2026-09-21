package dataaccess;

import business.models.Pedido;

import java.util.Map;

public class PedidoRepositoryMemoria implements PedidoRepository {
    private final Map<Integer, Pedido> baseDatosMemoria;
    private int secuenciaId = 1;

    public PedidoRepositoryMemoria(Map<Integer, Pedido> baseDatosMemoria) {
        this.baseDatosMemoria = baseDatosMemoria;
    }

    @Override
    public int guardar(Pedido pedido) {
        if (pedido.getId() <= 0) {
            pedido.setId(secuenciaId++);
        }
        baseDatosMemoria.put(pedido.getId(), pedido);
        return pedido.getId();
    }

    @Override
    public Pedido buscarPorId(int id) {
        return this.baseDatosMemoria.get(id);
    }
}
