package dataaccess.producto;

import business.models.Producto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion concreta de ProductoRepository que carga los productos
 * desde un archivo plano CSV/TXT delimitado por comas.
 */
public class ProductoRepositoryArchivo implements ProductoRepository {
    private static final String ARCHIVO_DEFECTO = "productos.csv";
    private final String rutaArchivo;

    public ProductoRepositoryArchivo() {
        this(ARCHIVO_DEFECTO);
    }

    public ProductoRepositoryArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            crearArchivoPorDefecto(archivo);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                if (primeraLinea && linea.toLowerCase().contains("nombre")) {
                    primeraLinea = false;
                    continue;
                }
                primeraLinea = false;

                String[] partes = linea.split(",");
                if (partes.length >= 3) {
                    String nombre = partes[0].trim();
                    double precio = Double.parseDouble(partes[1].trim());
                    int existencia = Integer.parseInt(partes[2].trim());
                    productos.add(new Producto(nombre, precio, 0, existencia));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error al cargar productos desde archivo: " + e.getMessage());
        }

        return productos;
    }

    private void crearArchivoPorDefecto(File archivo) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            writer.write("nombre,precio,existencia\n");
            writer.write("Laptop Gamer,1500.0,5\n");
            writer.write("Mouse Inalambrico,250.0,10\n");
            writer.write("Teclado Mecanico,350.0,8\n");
            writer.write("Monitor 4K,800.0,3\n");
            writer.write("Servidor Enterprise,6000.0,2\n");
            writer.write("Memoria USB 64GB,150.0,20\n");
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo crear el archivo por defecto de productos: " + e.getMessage());
        }
    }
}
