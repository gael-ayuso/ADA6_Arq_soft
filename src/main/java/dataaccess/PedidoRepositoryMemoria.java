package dataaccess;

import business.Pedido;
import data.BaseDatosMemoria;

import java.util.Map;

public class PedidoRepositoryMemoria implements PedidoRepository {
    private final BaseDatosMemoria baseDatosMemoria;

    public PedidoRepositoryMemoria(BaseDatosMemoria baseDatosMemoria) {
        this.baseDatosMemoria = baseDatosMemoria;
    }

    @Override
    public int guardar(Pedido pedido) {
        Map<Integer, Pedido> datos = this.baseDatosMemoria.pedidos();
        datos.put(pedido.getId(), pedido);
        return pedido.getId();
    }

    @Override
    public Pedido buscarPorId(int id) {
        return this.baseDatosMemoria.pedidos().get(id);
    }
}
