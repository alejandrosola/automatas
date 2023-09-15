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

import model.Automata;
import model.Estado;
import util.Helpers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectorAutomata {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tabla de Automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                            ex.printStackTrace();
                        }
                    }
                }
            });
            JTextField fileNameTextField = new JTextField(20);
            fileNameTextField.setEditable(false);

            JPanel fileSelectionPanel = new JPanel();
            fileSelectionPanel.add(openFileButton);
            frame.add(fileSelectionPanel, BorderLayout.SOUTH);
            frame.setSize(400, 300);
            frame.setVisible(true);
    }

    private static void mostrarAutomata(Automata automata) {
        try {

        JFrame frame = new JFrame("Tabla de Automata");

            // Crear un modelo de tabla
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Delta");
            for (String c : automata.getLenguaje()) {
                model.addColumn(c);
            }
            model.addColumn("F");

            // Crear la tabla
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            // Agregar estado inicial a la tabla (porque quiero que aparezca primero)
            for (Estado e : automata.getEstadosList()) {
                Map<String, String> destinos = new HashMap<>();
                for (String i : automata.getLenguaje()) {
                    for (Estado d : e.getDestinos(i)) {
                        if (destinos.get(i) != null) {
                            String temp = destinos.get(i);
                            destinos.put(i, temp + " " + d.getNombre());
                        } else {
                            destinos.put(i, d.getNombre());
                        }
                    }
                }
                String[] row = new String[automata.getLenguaje().size() + 2];
                row[0] = e.getNombre();
                int j = 1;
                for (String i : automata.getLenguaje()) {
                    row[j] = destinos.get(i);
                    j++;
                }
                row[row.length - 1] = e.isAceptador() ? "1": "0";
                model.addRow(row);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);



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
            }
            
            frame.add(southPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
            frame.setSize(400, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
