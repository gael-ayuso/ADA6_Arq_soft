package ui;

import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion grafica profesional de la capa de presentacion (Swing + FlatLaf).
 * Cumple estrictamente con las restricciones arquitectonicas de ADA6:
 * - Unicamente interactua con PedidoService.
 * - No calcula totales, subtotales ni impuestos.
 * - No accede a estructuras de almacenamiento ni repositorios.
 * - Libre de emojis para garantizar una interfaz formal e institucional.
 */
public class PedidoVistaGUI extends JFrame implements PedidoVista {
    private final PedidoService pedidoService;

    // Componentes para registro
    private JTextField txtCliente;
    private JComboBox<ProductoItem> cmbCatalogo;
    private JSpinner spnCantidad;
    private DefaultTableModel modeloTablaDetalle;
    private final List<Producto> productosOrdenActual = new ArrayList<>();
    private JLabel lblMensajeRegistro;

    // Componentes para consulta y listado
    private JTextField txtBuscarId;
    private DefaultTableModel modeloTablaPedidos;
    private JTable tblPedidos;
    private JTextArea txtResumenDetalle;

    public PedidoVistaGUI(PedidoService pedidoService) {
        super("Sistema de Procesamiento de Pedidos - Arquitectura en Capas");
        this.pedidoService = pedidoService;
    }

    @Override
    public void iniciar() {
        configurarLookAndFeel();
        SwingUtilities.invokeLater(this::construirYMostrar);
    }

    private void configurarLookAndFeel() {
        try {
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }

    private void construirYMostrar() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Encabezado
        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);

        // 2. Panel Central dividido: Registro (Izquierda) y Listado/Consulta (Derecha)
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                crearPanelRegistro(),
                crearPanelConsultaYListado()
        );
        splitPane.setDividerLocation(490);
        splitPane.setResizeWeight(0.45);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        setContentPane(panelPrincipal);
        refrescarTablaPedidos();
        setVisible(true);
    }

    private JPanel crearPanelEncabezado() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 70)),
                new EmptyBorder(5, 5, 10, 5)
        ));

        JLabel lblTitulo = new JLabel("Sistema de Procesamiento de Pedidos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel lblSubtitulo = new JLabel("Estilo Arquitectonico en Capas: Presentacion -> Negocio -> Acceso a Datos -> Almacenamiento");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);

        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSubtitulo, BorderLayout.SOUTH);
        return header;
    }

    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Registrar Nuevo Pedido"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Formulario superior
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cliente
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        txtCliente = new JTextField();
        formPanel.add(txtCliente, gbc);

        // Catalogo de productos (cargado dinamicamente via PedidoService)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Catalogo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        cmbCatalogo = new JComboBox<>();
        cargarCatalogo();
        formPanel.add(cmbCatalogo, gbc);

        // Cantidad y Botones
        JPanel panelCantYAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelCantYAdd.add(new JLabel("Cantidad:"));
        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spnCantidad.setPreferredSize(new Dimension(65, 26));
        panelCantYAdd.add(spnCantidad);

        JButton btnAgregarProd = new JButton("Agregar a la Orden");
        btnAgregarProd.addActionListener(e -> agregarProductoActual());
        panelCantYAdd.add(btnAgregarProd);

        JButton btnProductoPersonalizado = new JButton("Producto Manual");
        btnProductoPersonalizado.setToolTipText("Ingresar un producto personalizado no presente en el catalogo");
        btnProductoPersonalizado.addActionListener(e -> mostrarDialogoProductoPersonalizado());
        panelCantYAdd.add(btnProductoPersonalizado);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(panelCantYAdd, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Tabla de productos en la orden actual
        modeloTablaDetalle = new DefaultTableModel(new Object[]{"Producto", "Precio Unit.", "Cantidad", "Existencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblDetalle = new JTable(modeloTablaDetalle);
        JScrollPane scrollDetalle = new JScrollPane(tblDetalle);
        scrollDetalle.setPreferredSize(new Dimension(300, 180));

        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.add(new JLabel("Productos incluidos en este pedido:"), BorderLayout.NORTH);
        panelCentro.add(scrollDetalle, BorderLayout.CENTER);

        JButton btnQuitar = new JButton("Quitar Seleccionado");
        btnQuitar.addActionListener(e -> {
            int selected = tblDetalle.getSelectedRow();
            if (selected >= 0) {
                productosOrdenActual.remove(selected);
                modeloTablaDetalle.removeRow(selected);
            }
        });
        panelCentro.add(btnQuitar, BorderLayout.SOUTH);

        panel.add(panelCentro, BorderLayout.CENTER);

        // Boton Procesar y estado inferior
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        JButton btnProcesar = new JButton("Procesar y Guardar Pedido");
        btnProcesar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnProcesar.setPreferredSize(new Dimension(200, 38));
        btnProcesar.addActionListener(e -> ejecutarRegistroPedido());

        lblMensajeRegistro = new JLabel("Listo para registrar pedido.");
        lblMensajeRegistro.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panelInferior.add(btnProcesar, BorderLayout.NORTH);
        panelInferior.add(lblMensajeRegistro, BorderLayout.SOUTH);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelConsultaYListado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Consultar y Listar Pedidos"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Barra superior de busqueda
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.add(new JLabel("ID Pedido:"));
        txtBuscarId = new JTextField(6);
        topBar.add(txtBuscarId);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> ejecutarConsultaPorId());
        topBar.add(btnBuscar);

        JButton btnRefrescar = new JButton("Actualizar Tabla");
        btnRefrescar.addActionListener(e -> refrescarTablaPedidos());
        topBar.add(btnRefrescar);

        panel.add(topBar, BorderLayout.NORTH);

        // Tabla de pedidos registrados
        modeloTablaPedidos = new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Subtotal", "Descuento", "IVA", "Total", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPedidos = new JTable(modeloTablaPedidos);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPedidos.getSelectedRow() >= 0) {
                int id = (int) tblPedidos.getValueAt(tblPedidos.getSelectedRow(), 0);
                mostrarDetallePedido(pedidoService.buscarPorId(id));
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tblPedidos);
        scrollTabla.setPreferredSize(new Dimension(400, 220));

        // Panel de Resumen/Detalle inferior
        txtResumenDetalle = new JTextArea(8, 30);
        txtResumenDetalle.setEditable(false);
        txtResumenDetalle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollResumen = new JScrollPane(txtResumenDetalle);
        scrollResumen.setBorder(BorderFactory.createTitledBorder("Detalle del Pedido"));

        JSplitPane splitVertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                scrollTabla,
                scrollResumen
        );
        splitVertical.setDividerLocation(260);
        splitVertical.setResizeWeight(0.6);

        panel.add(splitVertical, BorderLayout.CENTER);
        return panel;
    }

    private void cargarCatalogo() {
        cmbCatalogo.removeAllItems();
        List<Producto> catalogo = pedidoService.obtenerCatalogoProductos();
        if (catalogo != null) {
            for (Producto prod : catalogo) {
                cmbCatalogo.addItem(new ProductoItem(prod));
            }
        }
    }

    private void agregarProductoActual() {
        ProductoItem item = (ProductoItem) cmbCatalogo.getSelectedItem();
        if (item == null) return;

        int cantidad = (int) spnCantidad.getValue();
        Producto base = item.producto;

        Producto nuevoProd = new Producto(
                base.getNombre(),
                base.getPrecio(),
                cantidad,
                base.getExistencia()
        );

        productosOrdenActual.add(nuevoProd);
        modeloTablaDetalle.addRow(new Object[]{
                nuevoProd.getNombre(),
                String.format("$%.2f", nuevoProd.getPrecio()),
                nuevoProd.getCantidad(),
                nuevoProd.getExistencia()
        });
        lblMensajeRegistro.setText("[INFO] Producto agregado a la orden.");
    }

    private void mostrarDialogoProductoPersonalizado() {
        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtCant = new JTextField("1");
        JTextField txtStock = new JTextField("10");

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Nombre del producto:"));
        form.add(txtNombre);
        form.add(new JLabel("Precio unitario:"));
        form.add(txtPrecio);
        form.add(new JLabel("Cantidad a solicitar:"));
        form.add(txtCant);
        form.add(new JLabel("Existencia en inventario:"));
        form.add(txtStock);

        int opt = JOptionPane.showConfirmDialog(
                this, form, "Ingresar Producto Personalizado",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre del producto no puede estar vacio.", "Error de Validacion", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                int cantidad = Integer.parseInt(txtCant.getText().trim());
                int existencia = Integer.parseInt(txtStock.getText().trim());

                Producto p = new Producto(nombre, precio, cantidad, existencia);
                productosOrdenActual.add(p);
                modeloTablaDetalle.addRow(new Object[]{
                        p.getNombre(),
                        String.format("$%.2f", p.getPrecio()),
                        p.getCantidad(),
                        p.getExistencia()
                });
                lblMensajeRegistro.setText("[INFO] Producto personalizado agregado a la orden.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio, cantidad o existencia no tienen formato numerico valido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ejecutarRegistroPedido() {
        String cliente = txtCliente.getText().trim();
        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del cliente no puede estar vacio.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (productosOrdenActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto a la orden.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pedido pedido = new Pedido(cliente, new ArrayList<>(productosOrdenActual));

        try {
            Pedido procesado = pedidoService.registrar(pedido);

            if (procesado != null) {
                lblMensajeRegistro.setText("[OK] Pedido #" + procesado.getId() + " registrado con exito.");
                JOptionPane.showMessageDialog(this,
                        "Pedido registrado correctamente.\nID: " + procesado.getId() + "\nTotal: $" + String.format("%.2f", procesado.getTotal()),
                        "Confirmacion", JOptionPane.INFORMATION_MESSAGE);

                txtCliente.setText("");
                productosOrdenActual.clear();
                modeloTablaDetalle.setRowCount(0);
                spnCantidad.setValue(1);

                refrescarTablaPedidos();
                mostrarDetallePedido(procesado);
            }
        } catch (Exception ex) {
            lblMensajeRegistro.setText("[ERROR] " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error de validacion o negocio:\n" + ex.getMessage(), "Error al Registrar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarConsultaPorId() {
        String idStr = txtBuscarId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID de pedido.", "Atencion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Pedido p = pedidoService.buscarPorId(id);
            if (p != null) {
                mostrarDetallePedido(p);
            } else {
                txtResumenDetalle.setText("[ERROR] No se encontro ningun pedido con ID: " + id);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un numero entero.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTablaPedidos() {
        modeloTablaPedidos.setRowCount(0);
        List<Pedido> pedidos = pedidoService.listarPedidos();
        if (pedidos != null) {
            for (Pedido p : pedidos) {
                modeloTablaPedidos.addRow(new Object[]{
                        p.getId(),
                        p.getCliente(),
                        String.format("$%.2f", p.getSubtotal()),
                        String.format("$%.2f", p.getDescuento()),
                        String.format("$%.2f", p.getImpuestos()),
                        String.format("$%.2f", p.getTotal()),
                        p.getEstado()
                });
            }
        }
    }

    private void mostrarDetallePedido(Pedido p) {
        if (p == null) {
            txtResumenDetalle.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append(String.format(" ID: %-5d | Cliente: %s%n", p.getId(), p.getCliente()));
        sb.append(String.format(" Estado: %-15s%n", p.getEstado()));
        sb.append("-----------------------------------------\n");
        sb.append(String.format(" Subtotal:       $%10.2f%n", p.getSubtotal()));
        sb.append(String.format(" Descuento (10%%):$%10.2f%n", p.getDescuento()));
        sb.append(String.format(" IVA (16%%):      $%10.2f%n", p.getImpuestos()));
        sb.append(String.format(" TOTAL A PAGAR:  $%10.2f%n", p.getTotal()));
        if (p.isRevisionFraude()) {
            sb.append(" [ALERTA] Marcado para revision de fraude (> $5,000)\n");
        }
        sb.append("-----------------------------------------\n");
        sb.append(" Productos incluidos:\n");
        if (p.getListaProductos() != null) {
            for (Producto prod : p.getListaProductos()) {
                sb.append(String.format("  - %-20s x %-3d ($%.2f c/u)%n",
                        prod.getNombre(), prod.getCantidad(), prod.getPrecio()));
            }
        }
        sb.append("=========================================");
        txtResumenDetalle.setText(sb.toString());
    }

    private static class ProductoItem {
        final Producto producto;

        ProductoItem(Producto producto) {
            this.producto = producto;
        }

        @Override
        public String toString() {
            return String.format("%s - $%.2f (Existencia: %d)",
                    producto.getNombre(), producto.getPrecio(), producto.getExistencia());
        }
    }
}
