package data;

import business.Pedido;

import java.util.HashMap;
import java.util.Map;

public record BaseDatosMemoria(Map<Integer, Pedido> pedidos) {
    public BaseDatosMemoria() {
        this(new HashMap<>());
    }
}
