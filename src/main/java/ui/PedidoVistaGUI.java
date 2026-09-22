package ui;

import business.PedidoService;
import business.models.Pedido;
import business.models.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoVistaGUI extends JFrame implements PedidoVista {
    private final PedidoService pedidoService;

    // Componentes para registro
    private JTextField txtCliente;
    private JComboBox<ProductoItem> cmbCatalogo;
    private JSpinner spnCantidad;
    private DefaultTableModel modeloTablaDetalle;
    private final List<Producto> productosOrdenActual = new ArrayList<>();
    private JLabel lblMensajeRegistro = new JLabel("Listo para registrar pedido.");

    // Componentes para consulta y listado
    private JTextField txtBuscarId;
    private DefaultTableModel modeloTablaPedidos;
    private JTable tblPedidos;

    // Componentes para el detalle grafico (reemplazo de terminal de texto)
    private CardLayout cardLayoutDetalle;
    private JPanel panelDetalleContenedor;
    private JLabel lblDetalleId;
    private JLabel lblDetalleCliente;
    private JLabel lblDetalleEstadoBadge;
    private JPanel pnlAlertaFraude;
    private JLabel lblValSubtotal;
    private JLabel lblValDescuento;
    private JLabel lblValImpuestos;
    private JLabel lblValTotal;
    private DefaultTableModel modeloTablaProductosDetalle;

    public PedidoVistaGUI(PedidoService pedidoService) {
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
        setSize(1350, 750);
        setMinimumSize(new Dimension(1000, 650));
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
        splitPane.setDividerLocation(470);
        splitPane.setResizeWeight(0.42);
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

        header.add(lblTitulo, BorderLayout.NORTH);
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
        gbc.insets = new Insets(5, 5, 5, 5);
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

        // Cantidad y Botones de accion
        JPanel panelCantYAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelCantYAdd.add(new JLabel("Cantidad:"));
        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spnCantidad.setPreferredSize(new Dimension(65, 28));
        panelCantYAdd.add(spnCantidad);

        // Boton Agregar a la orden (Azul corporativo)
        JButton btnAgregarProd = new JButton("Agregar a la Orden");
        btnAgregarProd.setBackground(new Color(30, 115, 190));
        btnAgregarProd.setForeground(Color.WHITE);
        btnAgregarProd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregarProd.addActionListener(e -> agregarProductoActual());
        panelCantYAdd.add(btnAgregarProd);

        // Boton Producto Personalizado (Gris pizarra neutral)
        JButton btnProductoPersonalizado = new JButton("Producto Manual");
        btnProductoPersonalizado.setBackground(new Color(75, 80, 88));
        btnProductoPersonalizado.setForeground(Color.WHITE);
        btnProductoPersonalizado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProductoPersonalizado.setToolTipText("Ingresar manualmente un producto no presente en el catalogo");
        btnProductoPersonalizado.addActionListener(e -> mostrarDialogoProductoPersonalizado());
        panelCantYAdd.add(btnProductoPersonalizado);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(panelCantYAdd, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Tabla de productos en la orden actual
        modeloTablaDetalle = new DefaultTableModel(new Object[]{"Producto", "Precio Unit.", "Cant.", "Subtotal"}, 0) {
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

        // Boton Quitar (Rojo sobrio para accion destructiva)
        JButton btnQuitar = new JButton("Quitar Seleccionado");
        btnQuitar.setBackground(new Color(165, 45, 45));
        btnQuitar.setForeground(Color.WHITE);
        btnQuitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuitar.addActionListener(e -> {
            int selected = tblDetalle.getSelectedRow();
            if (selected >= 0) {
                productosOrdenActual.remove(selected);
                modeloTablaDetalle.removeRow(selected);
            }
        });
        panelCentro.add(btnQuitar, BorderLayout.SOUTH);

        panel.add(panelCentro, BorderLayout.CENTER);

        // Boton Procesar Pedido (Verde esmeralda para accion principal de confirmacion)
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        JButton btnProcesar = new JButton("Procesar y Guardar Pedido");
        btnProcesar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnProcesar.setPreferredSize(new Dimension(200, 42));
        btnProcesar.setBackground(new Color(40, 160, 80));
        btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProcesar.addActionListener(e -> ejecutarRegistroPedido());

        lblMensajeRegistro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblMensajeRegistro.setForeground(Color.GRAY);

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
        btnBuscar.setBackground(new Color(30, 115, 190));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> ejecutarConsultaPorId());
        topBar.add(btnBuscar);

        JButton btnRefrescar = new JButton("Actualizar Tabla");
        btnRefrescar.setBackground(new Color(75, 80, 88));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        scrollTabla.setPreferredSize(new Dimension(450, 200));

        // Panel de Resumen/Detalle inferior (100% grafico)
        JPanel panelDetalleGrafico = crearPanelDetalleGrafico();

        JSplitPane splitVertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                scrollTabla,
                panelDetalleGrafico
        );
        splitVertical.setDividerLocation(230);
        splitVertical.setResizeWeight(0.45);

        panel.add(splitVertical, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye el panel grafico para visualizar el detalle del pedido seleccionado.
     * Reemplaza el antiguo cuadro de texto monospaciado por componentes visuales nativos.
     */
    private JPanel crearPanelDetalleGrafico() {
        cardLayoutDetalle = new CardLayout();
        panelDetalleContenedor = new JPanel(cardLayoutDetalle);
        panelDetalleContenedor.setBorder(BorderFactory.createTitledBorder("Detalle del Pedido"));

        // Vista 1: Estado Vacio (Placeholder inicial)
        JPanel panelVacio = new JPanel(new GridBagLayout());
        JLabel lblPlaceholder = new JLabel("Seleccione un pedido de la tabla superior o busque por ID para consultar su desglose.");
        lblPlaceholder.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPlaceholder.setForeground(Color.GRAY);
        panelVacio.add(lblPlaceholder);

        // Vista 2: Card con Detalle Grafico
        JPanel panelDetalle = new JPanel(new BorderLayout(10, 10));
        panelDetalle.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Header del pedido: ID, Cliente y Badge de Estado
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 5));
        
        JPanel pnlTitulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lblDetalleId = new JLabel("Pedido #0");
        lblDetalleId.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblDetalleCliente = new JLabel("Cliente: -");
        lblDetalleCliente.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDetalleCliente.setForeground(Color.LIGHT_GRAY);
        pnlTitulos.add(lblDetalleId);
        pnlTitulos.add(new JLabel("|"));
        pnlTitulos.add(lblDetalleCliente);

        lblDetalleEstadoBadge = new JLabel("PROCESADO");
        lblDetalleEstadoBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDetalleEstadoBadge.setForeground(Color.WHITE);
        lblDetalleEstadoBadge.setOpaque(true);
        lblDetalleEstadoBadge.setBackground(new Color(40, 140, 70));
        lblDetalleEstadoBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        pnlHeader.add(pnlTitulos, BorderLayout.WEST);
        pnlHeader.add(lblDetalleEstadoBadge, BorderLayout.EAST);

        // Alerta de fraude (visible solo si corresponde)
        pnlAlertaFraude = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pnlAlertaFraude.setBackground(new Color(130, 80, 20));
        JLabel lblTextoFraude = new JLabel("[ALERTA DE SEGURIDAD] Este pedido supera $5,000 y fue marcado para revision manual.");
        lblTextoFraude.setForeground(Color.WHITE);
        lblTextoFraude.setFont(new Font("SansSerif", Font.BOLD, 11));
        pnlAlertaFraude.add(lblTextoFraude);
        pnlAlertaFraude.setVisible(false);

        JPanel pnlNorteCompleto = new JPanel(new BorderLayout(0, 5));
        pnlNorteCompleto.add(pnlHeader, BorderLayout.NORTH);
        pnlNorteCompleto.add(pnlAlertaFraude, BorderLayout.SOUTH);
        panelDetalle.add(pnlNorteCompleto, BorderLayout.NORTH);

        // Centro: Tabla de productos del pedido
        modeloTablaProductosDetalle = new DefaultTableModel(new Object[]{"Producto", "Cant.", "Precio Unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblProductosDetalle = new JTable(modeloTablaProductosDetalle);
        JScrollPane scrollProds = new JScrollPane(tblProductosDetalle);
        scrollProds.setBorder(BorderFactory.createTitledBorder("Productos incluidos en este pedido:"));

        // Derecha / Sur: Desglose financiero estilizado
        JPanel pnlFinanciero = new JPanel(new GridBagLayout());
        pnlFinanciero.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 75, 80), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 6, 3, 6);

        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        pnlFinanciero.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        lblValSubtotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblValSubtotal.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pnlFinanciero.add(lblValSubtotal, gbc);

        // Descuento
        gbc.gridx = 0; gbc.gridy = 1;
        pnlFinanciero.add(new JLabel("Descuento (10%):"), gbc);
        gbc.gridx = 1;
        lblValDescuento = new JLabel("-$0.00", SwingConstants.RIGHT);
        lblValDescuento.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblValDescuento.setForeground(new Color(50, 185, 100)); // Verde destacado para descuento
        pnlFinanciero.add(lblValDescuento, gbc);

        // Impuestos
        gbc.gridx = 0; gbc.gridy = 2;
        pnlFinanciero.add(new JLabel("Impuestos (IVA 16%):"), gbc);
        gbc.gridx = 1;
        lblValImpuestos = new JLabel("+$0.00", SwingConstants.RIGHT);
        lblValImpuestos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pnlFinanciero.add(lblValImpuestos, gbc);

        // Linea divisoria
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        pnlFinanciero.add(sep, gbc);

        // Total Final
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblTotalEtiqueta = new JLabel("TOTAL A PAGAR:");
        lblTotalEtiqueta.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlFinanciero.add(lblTotalEtiqueta, gbc);
        gbc.gridx = 1;
        lblValTotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblValTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblValTotal.setForeground(new Color(230, 230, 230));
        pnlFinanciero.add(lblValTotal, gbc);

        // Disposicion: Productos en el centro, tarjeta financiera al este/derecha
        JPanel pnlCuerpo = new JPanel(new BorderLayout(10, 0));
        pnlCuerpo.add(scrollProds, BorderLayout.CENTER);
        pnlCuerpo.add(pnlFinanciero, BorderLayout.EAST);
        pnlFinanciero.setPreferredSize(new Dimension(280, 0));

        panelDetalle.add(pnlCuerpo, BorderLayout.CENTER);

        panelDetalleContenedor.add(panelVacio, "VACIO");
        panelDetalleContenedor.add(panelDetalle, "DETALLE");
        cardLayoutDetalle.show(panelDetalleContenedor, "VACIO");

        return panelDetalleContenedor;
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
                String.format("$%.2f", nuevoProd.getPrecio() * nuevoProd.getCantidad())
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
        form.add(new JLabel("Precio unitario ($):"));
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
                        String.format("$%.2f", p.getPrecio() * p.getCantidad())
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
                refrescarTablaPedidos();
                mostrarDetallePedido(procesado);

                JOptionPane.showMessageDialog(this,
                        "Pedido registrado correctamente.\nID: " + procesado.getId() + "\nTotal: $" + String.format("%.2f", procesado.getTotal()),
                        "Confirmacion", JOptionPane.INFORMATION_MESSAGE);

                txtCliente.setText("");
                productosOrdenActual.clear();
                modeloTablaDetalle.setRowCount(0);
                spnCantidad.setValue(1);
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
                cardLayoutDetalle.show(panelDetalleContenedor, "VACIO");
                JOptionPane.showMessageDialog(this, "No se encontro ningun pedido con ID: " + id, "Busqueda", JOptionPane.INFORMATION_MESSAGE);
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

    /**
     * Muestra de forma grafica y estructurada los datos del pedido en el panel de detalle.
     */
    private void mostrarDetallePedido(Pedido p) {
        if (p == null) {
            cardLayoutDetalle.show(panelDetalleContenedor, "VACIO");
            return;
        }

        lblDetalleId.setText("Pedido #" + p.getId());
        lblDetalleCliente.setText("Cliente: " + p.getCliente());
        lblDetalleEstadoBadge.setText(p.getEstado() != null ? p.getEstado().toString() : "PROCESADO");

        // Alerta de fraude
        pnlAlertaFraude.setVisible(p.isRevisionFraude());

        // Desglose financiero
        lblValSubtotal.setText(String.format("$%.2f", p.getSubtotal()));
        lblValDescuento.setText(String.format("-$%.2f", p.getDescuento()));
        lblValImpuestos.setText(String.format("+$%.2f", p.getImpuestos()));
        lblValTotal.setText(String.format("$%.2f", p.getTotal()));

        // Tabla de productos del pedido
        modeloTablaProductosDetalle.setRowCount(0);
        if (p.getListaProductos() != null) {
            for (Producto prod : p.getListaProductos()) {
                modeloTablaProductosDetalle.addRow(new Object[]{
                        prod.getNombre(),
                        prod.getCantidad(),
                        String.format("$%.2f", prod.getPrecio()),
                        String.format("$%.2f", prod.getPrecio() * prod.getCantidad())
                });
            }
        }

        cardLayoutDetalle.show(panelDetalleContenedor, "DETALLE");
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
