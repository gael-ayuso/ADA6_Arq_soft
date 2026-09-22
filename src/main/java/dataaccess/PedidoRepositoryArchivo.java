package dataaccess;

import business.models.EstadoPedido;
import business.models.Pedido;
import business.models.Producto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de persistencia de Pedidos basada en un archivo de texto plano.
 * Cumple con la segunda parte del requerimiento arquitectónico ADA6 para comprobar
 * la sustitución de componentes de persistencia sin alterar las capas superiores.
 */
public class PedidoRepositoryArchivo implements PedidoRepository {
    private static final String DEFAULT_FILE_PATH = "pedidos.txt";
    private final File archivo;

    public PedidoRepositoryArchivo() {
        this(DEFAULT_FILE_PATH);
    }

    public PedidoRepositoryArchivo(String rutaArchivo) {
        this.archivo = new File(rutaArchivo);
        inicializarArchivo();
    }

    private void inicializarArchivo() {
        try {
            if (!archivo.exists()) {
                File parent = archivo.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                archivo.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el archivo de almacenamiento: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized int guardar(Pedido pedido) {
        List<Pedido> pedidosActuales = listarTodos();

        if (pedido.getId() <= 0) {
            int maxId = 0;
            for (Pedido p : pedidosActuales) {
                if (p.getId() > maxId) {
                    maxId = p.getId();
                }
            }
            pedido.setId(maxId + 1);
        }

        // Actualizar si ya existe, o agregar si es nuevo
        boolean actualizado = false;
        for (int i = 0; i < pedidosActuales.size(); i++) {
            if (pedidosActuales.get(i).getId() == pedido.getId()) {
                pedidosActuales.set(i, pedido);
                actualizado = true;
                break;
            }
        }
        if (!actualizado) {
            pedidosActuales.add(pedido);
        }

        guardarTodosEnArchivo(pedidosActuales);
        return pedido.getId();
    }

    @Override
    public synchronized Pedido buscarPorId(int id) {
        List<Pedido> pedidos = listarTodos();
        for (Pedido p : pedidos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Pedido> listarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        if (!archivo.exists()) {
            return pedidos;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                Pedido p = deserializarPedido(linea);
                if (p != null) {
                    pedidos.add(p);
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo de pedidos: " + e.getMessage());
        }

        return pedidos;
    }

    private void guardarTodosEnArchivo(List<Pedido> pedidos) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo, false), StandardCharsets.UTF_8))) {
            for (Pedido p : pedidos) {
                writer.write(serializarPedido(p));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al persistir pedidos en archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Formato: ID|CLIENTE|SUBTOTAL|DESCUENTO|IMPUESTOS|TOTAL|ESTADO|REVISION_FRAUDE|PRODUCTOS
     * Donde cada producto es: nombre,precio,cantidad,existencia separados por ';'
     */
    private String serializarPedido(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.getId()).append("|")
          .append(escapar(p.getCliente())).append("|")
          .append(p.getSubtotal()).append("|")
          .append(p.getDescuento()).append("|")
          .append(p.getImpuestos()).append("|")
          .append(p.getTotal()).append("|")
          .append(p.getEstado() != null ? p.getEstado().name() : "").append("|")
          .append(p.isRevisionFraude()).append("|");

        List<Producto> productos = p.getListaProductos();
        if (productos != null && !productos.isEmpty()) {
            for (int i = 0; i < productos.size(); i++) {
                Producto prod = productos.get(i);
                sb.append(escapar(prod.getNombre())).append(",")
                  .append(prod.getPrecio()).append(",")
                  .append(prod.getCantidad()).append(",")
                  .append(prod.getExistencia());
                if (i < productos.size() - 1) {
                    sb.append(";");
                }
            }
        }
        return sb.toString();
    }

    private Pedido deserializarPedido(String linea) {
        try {
            String[] partes = linea.split("\\|", -1);
            if (partes.length < 8) {
                return null;
            }

            int id = Integer.parseInt(partes[0]);
            String cliente = desescapar(partes[1]);
            double subtotal = Double.parseDouble(partes[2]);
            double descuento = Double.parseDouble(partes[3]);
            double impuestos = Double.parseDouble(partes[4]);
            double total = Double.parseDouble(partes[5]);

            EstadoPedido estado = null;
            if (!partes[6].isEmpty()) {
                try {
                    estado = EstadoPedido.valueOf(partes[6]);
                } catch (IllegalArgumentException e) {
                    estado = EstadoPedido.PROCESADO;
                }
            }

            boolean revisionFraude = Boolean.parseBoolean(partes[7]);

            List<Producto> productos = new ArrayList<>();
            if (partes.length >= 9 && !partes[8].trim().isEmpty()) {
                String[] prodsStr = partes[8].split(";");
                for (String pStr : prodsStr) {
                    String[] datosProd = pStr.split(",");
                    if (datosProd.length >= 4) {
                        String nombreProd = desescapar(datosProd[0]);
                        double precio = Double.parseDouble(datosProd[1]);
                        int cantidad = Integer.parseInt(datosProd[2]);
                        int existencia = Integer.parseInt(datosProd[3]);
                        productos.add(new Producto(nombreProd, precio, cantidad, existencia));
                    }
                }
            }

            Pedido pedido = new Pedido(cliente, productos, subtotal, descuento, impuestos, total, estado);
            pedido.setId(id);
            pedido.setRevisionFraude(revisionFraude);
            return pedido;
        } catch (Exception e) {
            System.err.println("Error deserializando línea: " + linea + " -> " + e.getMessage());
            return null;
        }
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("|", "/").replace(";", ".").replace(",", ".");
    }

    private String desescapar(String valor) {
        return valor == null ? "" : valor;
    }
}
