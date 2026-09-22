import business.PedidoService;
import dataaccess.PedidoRepository;
import dataaccess.PedidoRepositoryArchivo;
import dataaccess.PedidoRepositoryMemoria;
import ui.PedidoVista;
import ui.PedidoVistaConsola;
import ui.PedidoVistaGUI;

public class Main {
    public static void main(String[] args) {
        // 1. CAPA DE ACCESO A DATOS
        // Para alternar entre persistencia en memoria y archivo
        // cambiar la instanciacion aqui:
        // PedidoRepository repositorio = new PedidoRepositoryMemoria();
        PedidoRepository repositorio = new PedidoRepositoryArchivo("pedidos.txt");

        // 2. CAPA DE LOGICA DE NEGOCIO
        // Depende del contrato PedidoRepository, no de la implementacion concreta.
        PedidoService pedidoService = new PedidoService(repositorio);

        // 3. CAPA DE PRESENTACION
        // Puede iniciarse en modo grafico o consola interactiva.
        PedidoVista vista;
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("--consola")) {
            vista = new PedidoVistaConsola(pedidoService);
        } else {
            vista = new PedidoVistaGUI(pedidoService);
        }

        vista.iniciar();
    }
}
