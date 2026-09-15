package business;

import dataaccess.PedidoRepository;
import dataaccess.PedidoRepositoryMemoria;

public class PedidoService {
    private final PedidoRepository pedidoRepository;
    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }
}
