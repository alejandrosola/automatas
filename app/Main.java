package app;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import model.Automata;
import model.Estado;
import util.Constantes;
import util.Helpers;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tabla de Automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel title = new JLabel("Seleccione el autómata (csv)");
        JButton openFileButton = new JButton("Seleccionar Archivo");
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String fileName = selectedFile.getAbsolutePath();
                    try {
                        Automata automata = Helpers.readAutomataFromCSV(fileName);
                        mostrarAutomata(automata);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        });
        JTextField fileNameTextField = new JTextField(20);
        fileNameTextField.setEditable(false);

        JPanel fileSelectionPanel = new JPanel();
        fileSelectionPanel.add(title);
        fileSelectionPanel.add(openFileButton);
        frame.add(fileSelectionPanel, BorderLayout.SOUTH);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private static void mostrarAutomata(Automata automata) {
        try {
            JFrame frame = new JFrame("Autómata");
            Font fuente = new Font("Arial", Font.PLAIN, 15);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Delta");
            for (String i : automata.getLenguaje()) {
                model.addColumn(i);
            }
            model.addColumn("F");

            JTable table = new JTable(model);
            table.setFont(fuente);
            JScrollPane scrollPane = new JScrollPane(table);

            // Agregar estado inicial a la tabla (porque quiero que aparezca primero)
            for (Estado e : automata.getEstadosList()) {
                Map<String, String> destinos = new HashMap<>();
                for (String i : automata.getLenguaje()) {
                    for (Estado d : e.getDestinos(i)) {
                        String destino = d != null ? d.getNombre() : "-";
                        if (destinos.get(i) != null) {
                            String temp = destinos.get(i);
                            destinos.put(i, temp + " " + destino);
                        } else {
                            destinos.put(i, destino);
                        }
                    }
                }
                String[] row = new String[automata.getLenguaje().size() + 2];
                if (!e.getNombre().equals(Constantes.ERROR)) {
                    row[0] = e.getNombre();
                    int j = 1;
                    for (String i : automata.getLenguaje()) {
                        row[j] = destinos.get(i);
                        j++;
                    }
                    row[row.length - 1] = e.isAceptador() ? "1" : "0";
                    model.addRow(row);
                }
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(200);

            frame.add(scrollPane, BorderLayout.CENTER);

            JPanel southPanel = new JPanel();
            southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
            if (!automata.isDeterministico()) {
                JButton calcularDeterministico = new JButton("Calcular determinístico");
                calcularDeterministico.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Automata deterministicoEquivalente = automata.getDeterministicoEquivalente();
                        mostrarAutomata(deterministicoEquivalente);
                    }
                });
                southPanel.add(calcularDeterministico);
            } else {
                JLabel label = new JLabel("Inserte una cadena separada por espacios: ");
                JTextField input = new JTextField();
                southPanel.add(label);
                southPanel.add(input);
                JButton confirmar = new JButton("Confirmar cadena");
                JLabel resultado = new JLabel();
                confirmar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String[] cadena = input.getText().split(" ");
                        try {
                            if (automata.isInputAceptado(Arrays.asList(cadena))) {
                                resultado.setText("Cadena aceptada");
                            } else {
                                resultado.setText("Cadena rechazada");
                            }
                        } catch (Exception ex) {
                            resultado.setText(ex.getMessage());
                        }
                    }
                });
                southPanel.add(confirmar);
                southPanel.add(resultado);

                JButton minimizar = new JButton("Minimizar");
                minimizar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            mostrarAutomata(automata.getMinimizado());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        // mostrarAutomata(deterministicoEquivalente);
                    }
                });
                southPanel.add(minimizar);
            }

            JButton visualizar = new JButton("Visualizar");
            visualizar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Graph<String, String> grafo = automata.getGrafo();
                    System.out.println(grafo);

                    Layout<String, String> layout = new CircleLayout(grafo);
                    layout.setSize(new Dimension(300, 300)); // sets the initial size of the space
                    // The BasicVisualizationServer<V,E> is parameterized by the edge types
                    VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
                    vv.setPreferredSize(new Dimension(350, 350)); // Sets the viewing area size
                    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                    vv.getRenderContext().setEdgeLabelTransformer(new EdgeLabeller());
                    vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
                    vv.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        public Paint transform(String vertex) {
                            // Cambiar el color del vértice "A" a rojo, los demás en negro
                            if (vertex.equals(automata.getEstadoInicial().getNombre())) {
                                return Color.cyan;
                            } else {
                                return Color.WHITE;
                            }
                        }
                    };

                    Transformer<String, Stroke> edgeStroke = new Transformer<String, Stroke>() {
                        public Stroke transform(String v) {
                            // Cambiar el grosor de las aristas conectadas al vértice "A" a un grosor más
                            // grueso
                            if (automata.getEstado(v).isAceptador()) {
                                return new BasicStroke(2.0f); // Grosor más grueso
                            } else {
                                return new BasicStroke(1.0f); // Grosor normal
                            }
                        }
                    };

                    // Asignar el Transformer al renderizador de vértices
                    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
                    vv.getRenderContext().setVertexStrokeTransformer(edgeStroke);

                    DefaultModalGraphMouse<Integer, String> gm = new DefaultModalGraphMouse<>();
                    vv.setGraphMouse(gm);

                    JFrame grafoFrame = new JFrame("Grafo del autómata");
                    grafoFrame.getContentPane().add(vv);
                    grafoFrame.pack();
                    grafoFrame.setVisible(true);
                    grafoFrame.setSize(400, 300);
                }
            });
            southPanel.add(visualizar);

            frame.add(southPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
            frame.setSize(400, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
