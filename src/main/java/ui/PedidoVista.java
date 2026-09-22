package ui;

/**
 * Contrato para la capa de presentación (Principio de Inversión de Dependencias - DIP).
 * Permite desacoplar la lógica de negocio de la tecnología concreta de interfaz de usuario
 * (consola, GUI con Swing/FlatLaf, web, etc.).
 */
public interface PedidoVista {
    /**
     * Inicia la interfaz de usuario (bucle de consola o visualización de ventana gráfica).
     */
    void iniciar();
}
